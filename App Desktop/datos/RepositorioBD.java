package app.datos;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import com.mongodb.client.model.Collation;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Sorts.ascending;

import app.constantes.Constantes;
import app.modelo.Pregunta;
import app.modelo.ProgresoTest;
import app.modelo.Tema;
import app.modelo.Test;
import app.modelo.Usuario;

/**
 * Capa de acceso a datos: contiene toda la lógica de consultas y escrituras a
 * MongoDB que antes vivía dentro de MainGUI. La interfaz gráfica nunca toca
 * Mongo directamente, solo llama a los métodos de esta clase.
 * 
 * @author Ángel Hernández
 */
public class RepositorioBD {

	// Caché en memoria para la carga perezosa: idTema -> sus Tests ya resueltos,
	// idTest -> sus Preguntas ya resueltas. Evita repetir consultas a Mongo cada
	// vez que el alumno vuelve a entrar al mismo Tema/Test.
	private static final Map<String, List<Test>> cacheTestsPorTema = new HashMap<>();
	private static final Map<String, List<Pregunta>> cachePreguntasPorTest = new HashMap<>();

	/** Clase de solo métodos estáticos: no debe instanciarse. */
	private RepositorioBD() {
	}

	/**
	 * Indica si existe conexión activa con la base de datos.
	 * 
	 * @return {@code true} si la colección de usuarios está disponible.
	 */
	public static boolean hayConexion() {
		return ConexionBD.obtenerColeccionUsuarios() != null;
	}

	/**
	 * Consulta un usuario específico en MongoDB y mapea su documento a un objeto
	 * Java, comprobando de paso si su licencia ha caducado.
	 * 
	 * @param dni El DNI del usuario a buscar.
	 * @return Una instancia de {@link Usuario} poblada con sus datos, o
	 *         {@code null} si no existe o hay error de conexión.
	 */
	public static Usuario obtenerUsuario(String dni) {
		try {
			MongoCollection<Document> coleccionUsuarios = ConexionBD.obtenerColeccionUsuarios();
			if (coleccionUsuarios == null) {
				return null;
			}

			Document doc = coleccionUsuarios.find(eq("_id", dni)).first();
			if (doc == null) {
				return null;
			}

			LocalDateTime dateLicencia = aLocalDateTime(doc.getDate("FechaLicencia"));
			LocalDateTime dateCaducidad = aLocalDateTime(doc.getDate("FechaCaducidad"));

			boolean licenciaActivaBD = comprobarYActualizarCaducidad(coleccionUsuarios, doc, dni, dateCaducidad);
			boolean esAdminBD = doc.getBoolean("Administrador", false);
			String nombre = doc.getString("Nombre");

			if (nombre == null) {
				nombre = "";
			}

			Map<String, ProgresoTest> progreso = new HashMap<>();
			Document docProgreso = doc.get("Progreso", Document.class);
			if (docProgreso != null) {
				for (String idTest : docProgreso.keySet()) {
					Document p = docProgreso.get(idTest, Document.class);
					List<String> falladas = p.getList("PreguntasFalladas", String.class);

					Map<String, Integer> respuestasUltimoIntento = new HashMap<>();
					Document docRespuestas = p.get("RespuestasUltimoIntento", Document.class);
					if (docRespuestas != null) {
						for (String idPregunta : docRespuestas.keySet()) {
							respuestasUltimoIntento.put(idPregunta, docRespuestas.getInteger(idPregunta));
						}
					}

					progreso.put(idTest, new ProgresoTest(p.getInteger("Fallos", 0),
							new HashSet<>(falladas != null ? falladas : new ArrayList<>()), respuestasUltimoIntento));
				}
			}

			return new Usuario(dni, licenciaActivaBD, esAdminBD, dateLicencia, dateCaducidad, nombre, progreso);
		} catch (Exception e) {
			System.err.println("Error al consultar MongoDB: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Verifica si la fecha actual ha superado la fecha de caducidad de la licencia
	 * del usuario. De ser así, actualiza su estado en la base de datos para revocar
	 * el acceso.
	 * 
	 * @param coleccionUsuarios La colección de usuarios donde aplicar el update.
	 * @param doc               El documento de MongoDB correspondiente al usuario.
	 * @param dni               El identificador (DNI) del usuario.
	 * @param fechaCaducidad    La fecha límite de la licencia parseada en UTC.
	 * @return {@code true} si la licencia sigue siendo válida, {@code false} si ha
	 *         caducado.
	 */
	private static boolean comprobarYActualizarCaducidad(MongoCollection<Document> coleccionUsuarios, Document doc,
			String dni, LocalDateTime fechaCaducidad) {
		boolean estadoLicencia = doc.getBoolean("LicenciaActiva", false);
		if (fechaCaducidad == null || !estadoLicencia) {
			return estadoLicencia;
		}
		if (LocalDateTime.now(ZoneOffset.UTC).isAfter(fechaCaducidad)) {
			try {
				coleccionUsuarios.updateOne(eq("_id", dni),
						new Document("$set", new Document("LicenciaActiva", false)));
				estadoLicencia = false;
			} catch (Exception e) {
				System.err.println("Error al comprobar caducidad: " + e.getMessage());
			}
		}
		return estadoLicencia;
	}

	/**
	 * Crea un usuario nuevo (con 30 días de licencia) o, si ya existe, renueva su
	 * licencia sumando {@value Constantes#DIAS_VALIDEZ_LICENCIA} días a su fecha de
	 * caducidad actual. Además, suma 1 a "LicenciasVendidas" del administrador que
	 * realiza la operación.
	 * 
	 * <p>
	 * FIX: al crear un alumno nuevo, "usuarioBD" (el propio alumno) es siempre
	 * {@code null} -es justo lo que hace que "creado" sea {@code true}-, así que no
	 * tiene "Nombre" propio del que tirar todavía. El nombre de la Autoescuela que
	 * se guarda en el alumno nuevo se toma del administrador logueado ("dniAdmin"),
	 * consultándolo aparte.
	 * 
	 * @param dni      El DNI del usuario a crear o renovar.
	 * @param dniAdmin El DNI del administrador logueado que realiza la operación.
	 * @return La nueva fecha de caducidad de la licencia, ya actualizada.
	 * @throws IllegalStateException Si no hay conexión con la base de datos.
	 */
	public static LocalDateTime renovarOcrearLicencia(String dni, String dniAdmin) {
		MongoCollection<Document> coleccionUsuarios = ConexionBD.obtenerColeccionUsuarios();
		if (coleccionUsuarios == null) {
			throw new IllegalStateException("Sin conexión a la base de datos.");
		}

		Usuario usuarioBD = obtenerUsuario(dni);
		LocalDateTime ahoraUTC = LocalDateTime.now(ZoneOffset.UTC);
		boolean creado = (usuarioBD == null);
		LocalDateTime nuevaFechaCaducidad;

		if (creado) {
			Usuario adminBD = obtenerUsuario(dniAdmin);
			String nombreAutoescuela = (adminBD != null) ? adminBD.getNombre() : "";

			nuevaFechaCaducidad = ahoraUTC.plusDays(Constantes.DIAS_VALIDEZ_LICENCIA);
			Document nuevoUsuario = new Document("_id", dni).append("LicenciaActiva", true)
					.append("FechaLicencia", aDate(ahoraUTC)).append("FechaCaducidad", aDate(nuevaFechaCaducidad))
					.append("Autoescuela", nombreAutoescuela).append("Progreso", new Document());
			coleccionUsuarios.insertOne(nuevoUsuario);
		} else {
			nuevaFechaCaducidad = (usuarioBD.isLicenciaActiva() && usuarioBD.getFechaCaducidad() != null)
					? usuarioBD.getFechaCaducidad().plusDays(Constantes.DIAS_VALIDEZ_LICENCIA)
					: ahoraUTC.plusDays(Constantes.DIAS_VALIDEZ_LICENCIA);

			coleccionUsuarios.updateOne(eq("_id", dni), new Document("$set",
					new Document("LicenciaActiva", true).append("FechaCaducidad", aDate(nuevaFechaCaducidad))));
		}

		coleccionUsuarios.updateOne(eq("_id", dniAdmin), new Document("$inc", new Document("LicenciasVendidas", 1)));
		return nuevaFechaCaducidad;
	}

	// ===== Temas / Tests / Preguntas: carga perezosa con caché =====

	/**
	 * Extrae únicamente los Temas (id, título e ids de sus Tests) desde MongoDB. A
	 * diferencia de versiones anteriores, NO trae también los Tests ni las
	 * Preguntas: eso se pide a demanda con {@link #obtenerTestsDeTema(Tema)} y
	 * {@link #obtenerPreguntasDeTest(Test)} para no cargar toda la base de datos de
	 * golpe al arrancar.
	 * 
	 * @return La lista de {@link Tema} disponibles, vacía si hay error o no hay
	 *         conexión.
	 */
	public static List<Tema> cargarTemas() {
		List<Tema> listaTemas = new ArrayList<>();
		try {
			MongoCollection<Document> coleccionTemas = ConexionBD.obtenerColeccionTemas();
			if (coleccionTemas == null) {
				return listaTemas;
			}
			for (Document docTema : coleccionTemas.find().sort(ascending("_id"))
					.collation(Collation.builder().locale("es").numericOrdering(true).build())) {
				listaTemas.add(new Tema(docTema.getString("_id"), docTema.getString("Titulo"),
						docTema.getList("Tests", String.class)));
			}
		} catch (Exception e) {
			System.err.println("Error al extraer temas: " + e.getMessage());
		}
		return listaTemas;
	}

	/**
	 * Devuelve los {@link Test} de un Tema. La primera vez que se pide un Tema
	 * concreto se hace una única consulta en lote a MongoDB (ver
	 * {@link #indexarPorId}) y el resultado se guarda en caché; las siguientes
	 * veces se devuelve directamente desde memoria.
	 * 
	 * @param tema El Tema cuyos Tests se quieren obtener.
	 * @return La lista de {@link Test} del tema, vacía si no tiene o hay error.
	 */
	public static List<Test> obtenerTestsDeTema(Tema tema) {
		return cacheTestsPorTema.computeIfAbsent(tema.getId(), idTema -> {
			MongoCollection<Document> coleccionTests = ConexionBD.obtenerColeccionTests();
			List<Test> resultado = new ArrayList<>();
			List<String> idsTests = tema.getIdsTests();
			if (coleccionTests == null || idsTests == null) {
				return resultado;
			}

			Map<String, Document> mapaTests = indexarPorId(coleccionTests, new HashSet<>(idsTests));
			for (String idTest : idsTests) {
				Document docTest = mapaTests.get(idTest);
				if (docTest == null) {
					System.err.println("Aviso: Test no encontrado, referencia ignorada -> " + idTest);
					continue;
				}
				resultado.add(new Test(idTest, docTest.getList("Preguntas", String.class)));
			}
			return resultado;
		});
	}

	/**
	 * Devuelve las {@link Pregunta} de un Test. La primera vez que se pide un Test
	 * concreto se hace una única consulta en lote a MongoDB (ver
	 * {@link #indexarPorId}) y el resultado se guarda en caché; las siguientes
	 * veces se devuelve directamente desde memoria.
	 * 
	 * @param test El Test cuyas Preguntas se quieren obtener.
	 * @return La lista de {@link Pregunta} del test, vacía si no tiene o hay error.
	 */
	public static List<Pregunta> obtenerPreguntasDeTest(Test test) {
		return cachePreguntasPorTest.computeIfAbsent(test.getId(), idTest -> {
			MongoCollection<Document> coleccionPreguntas = ConexionBD.obtenerColeccionPreguntas();
			List<Pregunta> resultado = new ArrayList<>();
			List<String> idsPreguntas = test.getIdsPreguntas();
			if (coleccionPreguntas == null || idsPreguntas == null) {
				return resultado;
			}

			Map<String, Document> mapaPreguntas = indexarPorId(coleccionPreguntas, new HashSet<>(idsPreguntas));
			for (String idPregunta : idsPreguntas) {
				Document docPregunta = mapaPreguntas.get(idPregunta);
				if (docPregunta == null) {
					System.err.println("Aviso: Pregunta no encontrada, referencia ignorada -> " + idPregunta);
					continue;
				}
				resultado.add(new Pregunta(docPregunta.getString("_id"), docPregunta.getString("Enunciado"),
						docPregunta.getList("Opciones", String.class),
						docPregunta.getInteger("IndiceRespuestaCorrecta"), docPregunta.getString("Explicacion"),
						docPregunta.getBoolean("Imagen", false)));
			}

			return resultado;
		});
	}

	/**
	 * Trae de una sola vez (consulta $in) todos los documentos cuyo _id está en
	 * "ids" y los indexa en un mapa, para evitar una consulta por documento.
	 * 
	 * @param coleccion La colección de MongoDB sobre la que se realiza la consulta
	 *                  masiva.
	 * @param ids       El conjunto de identificadores únicos a buscar.
	 * @return Un mapa que asocia cada identificador con su respectivo Documento.
	 */
	private static Map<String, Document> indexarPorId(MongoCollection<Document> coleccion, Set<String> ids) {
		Map<String, Document> mapa = new HashMap<>();
		if (ids.isEmpty()) {
			return mapa;
		}
		for (Document doc : coleccion.find(in("_id", ids))) {
			mapa.put(doc.getString("_id"), doc);
		}
		return mapa;
	}

	/**
	 * Convierte una fecha de MongoDB ({@link Date}) a {@link LocalDateTime} en UTC.
	 * 
	 * @param fecha La fecha tal cual la devuelve el driver de Mongo.
	 * @return La fecha convertida, o {@code null} si "fecha" era {@code null}.
	 */
	private static LocalDateTime aLocalDateTime(Date fecha) {
		return fecha != null ? fecha.toInstant().atZone(ZoneOffset.UTC).toLocalDateTime() : null;
	}

	/**
	 * Convierte un {@link LocalDateTime} en UTC al {@link Date} que espera Mongo.
	 * 
	 * @param ldt La fecha en formato Java moderno.
	 * @return La fecha convertida al tipo que acepta el driver de Mongo.
	 */
	private static Date aDate(LocalDateTime ldt) {
		return Date.from(ldt.toInstant(ZoneOffset.UTC));
	}

	/**
	 * Persiste en MongoDB el progreso de un Test ya corregido en modo Estudio o
	 * "Test de errores": fallos y preguntas falladas pendientes. Usa rutas de campo
	 * específicas (no reemplaza el subdocumento completo) para no borrar
	 * {@code RespuestasUltimoIntento} si ya existía de un intento previo en modo
	 * Examen.
	 * 
	 * @param dni                  El DNI del alumno cuyo progreso se actualiza.
	 * @param idTest               El identificador del Test corregido.
	 * @param fallos               El número de preguntas falladas en esta
	 *                             corrección.
	 * @param idsPreguntasFalladas El conjunto actualizado de IDs de preguntas
	 *                             falladas pendientes de repaso.
	 */
	public static void guardarProgresoTest(String dni, String idTest, int fallos, Set<String> idsPreguntasFalladas) {
		guardarProgresoTest(dni, idTest, fallos, idsPreguntasFalladas, null);
	}

	/**
	 * Igual que {@link #guardarProgresoTest(String, String, int, Set)}, pero además
	 * persiste {@code RespuestasUltimoIntento}, usada tras un intento en modo
	 * Examen para alimentar el modo Repaso de ese Test. Si
	 * "respuestasUltimoIntento" es {@code null}, ese campo no se toca (caso
	 * Estudio/errores).
	 *
	 * @param dni                     El DNI del alumno cuyo progreso se actualiza.
	 * @param idTest                  El identificador del Test corregido.
	 * @param fallos                  El número de preguntas falladas en esta
	 *                                corrección.
	 * @param idsPreguntasFalladas    El conjunto actualizado de IDs de preguntas
	 *                                falladas pendientes de repaso.
	 * @param respuestasUltimoIntento Mapa idPregunta -> índice de opción elegida de
	 *                                este intento en modo Examen, o {@code null} si
	 *                                no aplica.
	 */
	public static void guardarProgresoTest(String dni, String idTest, int fallos, Set<String> idsPreguntasFalladas,
			Map<String, Integer> respuestasUltimoIntento) {
		MongoCollection<Document> coleccionUsuarios = ConexionBD.obtenerColeccionUsuarios();
		if (coleccionUsuarios == null) {
			return;
		}
		try {
			String prefijo = "Progreso." + idTest + ".";
			Document camposActualizados = new Document(prefijo + "Fallos", fallos).append(prefijo + "PreguntasFalladas",
					new ArrayList<>(idsPreguntasFalladas));

			if (respuestasUltimoIntento != null) {
				Document docRespuestas = new Document();
				for (Map.Entry<String, Integer> entrada : respuestasUltimoIntento.entrySet()) {
					docRespuestas.append(entrada.getKey(), entrada.getValue());
				}
				camposActualizados.append(prefijo + "RespuestasUltimoIntento", docRespuestas);
			}

			coleccionUsuarios.updateOne(eq("_id", dni), new Document("$set", camposActualizados));
		} catch (Exception e) {
			System.err.println("Error al guardar progreso: " + e.getMessage());
		}
	}

	/**
	 * Trae de MongoDB las {@link Pregunta} correspondientes a una lista de IDs
	 * sueltos (no agrupados por Test), usada por el modo "Test de errores" para
	 * construir un examen con preguntas de distintos Tests. Reutiliza
	 * {@link #indexarPorId} para resolverlas en una sola consulta en lote, pero sin
	 * pasar por la caché de {@code cachePreguntasPorTest} ya que estas preguntas no
	 * pertenecen a un único Test.
	 *
	 * @param idsPreguntas Los IDs de las preguntas a recuperar.
	 * @return La lista de {@link Pregunta} encontradas, vacía si no hay conexión,
	 *         la lista es {@code null}/vacía, o ninguna coincide.
	 */
	public static List<Pregunta> obtenerPreguntasPorIds(List<String> idsPreguntas) {
		List<Pregunta> resultado = new ArrayList<>();
		MongoCollection<Document> coleccionPreguntas = ConexionBD.obtenerColeccionPreguntas();
		if (coleccionPreguntas == null || idsPreguntas == null || idsPreguntas.isEmpty()) {
			return resultado;
		}
		Map<String, Document> mapaPreguntas = indexarPorId(coleccionPreguntas, new HashSet<>(idsPreguntas));
		for (String idPregunta : idsPreguntas) {
			Document docPregunta = mapaPreguntas.get(idPregunta);
			if (docPregunta == null) {
				continue;
			}
			resultado.add(new Pregunta(docPregunta.getString("_id"), docPregunta.getString("Enunciado"),
					docPregunta.getList("Opciones", String.class), docPregunta.getInteger("IndiceRespuestaCorrecta"),
					docPregunta.getString("Explicacion"), docPregunta.getBoolean("Imagen", false)));
		}
		return resultado;
	}

	/**
	 * Consulta en MongoDB una opción del menú principal (test aleatorio o test de
	 * errores) y la mapea como un {@link Tema} normal, para poder reutilizar sin
	 * cambios todo el flujo de {@link #obtenerTestsDeTema(Tema)} y su caché.
	 * 
	 * @param idOpcion El identificador de la opción a buscar (ej.
	 *                 {@link Constantes#ID_OPCION_ALEATORIOS}).
	 * @return El {@link Tema} construido a partir del documento, o {@code null} si
	 *         no existe o hay error de conexión.
	 */
	public static Tema cargarOpcion(String idOpcion) {
		try {
			MongoCollection<Document> coleccionOpciones = ConexionBD.obtenerColeccionOpciones();
			if (coleccionOpciones == null) {
				return null;
			}
			Document doc = coleccionOpciones.find(eq("_id", idOpcion)).first();
			if (doc == null) {
				return null;
			}
			return new Tema(doc.getString("_id"), doc.getString("Titulo"), doc.getList("Tests", String.class));
		} catch (Exception e) {
			System.err.println("Error al cargar opción " + idOpcion + ": " + e.getMessage());
			return null;
		}
	}
}
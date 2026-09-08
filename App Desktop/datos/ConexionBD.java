package app.datos;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

import org.bson.Document;

import app.constantes.Constantes;

/**
 * Clase encargada de gestionar la conexión y el ciclo de vida de la base de
 * datos MongoDB Atlas. Proporciona acceso centralizado a las distintas
 * colecciones del sistema de autoescuela. La URI de conexión nunca está escrita
 * en el código: se lee de una variable de entorno o de un archivo de
 * configuración local externo (ver {@link #obtenerUriConexion()}).
 * 
 * @author Ángel Hernández
 */
public class ConexionBD {

	private static MongoClient clienteMongo;
	private static MongoDatabase baseDatos;

	/** Clase de solo métodos estáticos: no debe instanciarse. */
	private ConexionBD() {
	}

	// Bloque estático: se ejecuta automáticamente al cargar la clase en memoria
	static {
		try {
			String uriConexion = obtenerUriConexion();
			clienteMongo = MongoClients.create(uriConexion);
			baseDatos = clienteMongo.getDatabase(Constantes.NOMBRE_BD);
			System.out.println("¡Conexión con MongoDB Atlas establecida con éxito!");
		} catch (Exception e) {
			System.err.println("ERROR CRÍTICO: " + e.getMessage());
		}
	}

	/**
	 * Obtiene la cadena de conexión (URI) necesaria para conectar con MongoDB
	 * Atlas. Intenta leer primero desde las variables de entorno del sistema y, si
	 * no existe, recurre al archivo de configuración local externo. Al leer el
	 * archivo, se ignora el BOM (marca de orden de bytes) que añaden algunos
	 * editores de Windows (Notepad, VSCode) al guardar, y la búsqueda de la clave
	 * dentro del archivo no distingue mayúsculas/minúsculas ni espacios sobrantes.
	 * 
	 * @return La URI de conexión a la base de datos en formato String.
	 * @throws IOException Si no se encuentra la variable de entorno ni el archivo
	 *                     de configuración, o si la clave de la URI está vacía
	 *                     dentro del archivo.
	 */
	private static String obtenerUriConexion() throws IOException {
		String uriEntorno = System.getenv(Constantes.VARIABLE_ENTORNO_URI);
		if (uriEntorno != null && !uriEntorno.isBlank()) {
			return uriEntorno;
		}

		String contenido;
		try (InputStream entrada = ConexionBD.class.getResourceAsStream(Constantes.ARCHIVO_CONFIG)) {
			if (entrada == null) {
				throw new IOException("No se encontró la variable de entorno " + Constantes.VARIABLE_ENTORNO_URI
						+ " ni el recurso '" + Constantes.ARCHIVO_CONFIG
						+ "' en el classpath (debe estar en el mismo paquete que ConexionBD). Revisa el README para configurarlo.");
			}
			contenido = new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
		}

		if (!contenido.isEmpty() && contenido.charAt(0) == '\uFEFF') {
			contenido = contenido.substring(1);
		}

		Properties propiedades = new Properties();
		propiedades.load(new StringReader(contenido));

		String uriArchivo = propiedades.getProperty(Constantes.CLAVE_URI_CONFIG);
		if (uriArchivo == null) {
			for (String clave : propiedades.stringPropertyNames()) {
				if (clave.replace("\uFEFF", "").trim().equalsIgnoreCase(Constantes.CLAVE_URI_CONFIG)) {
					uriArchivo = propiedades.getProperty(clave);
					break;
				}
			}
		}

		if (uriArchivo == null || uriArchivo.isBlank()) {
			throw new IOException("La clave '" + Constantes.CLAVE_URI_CONFIG + "' no existe en "
					+ Constantes.ARCHIVO_CONFIG + ". Revisa que la línea sea exactamente: "
					+ Constantes.CLAVE_URI_CONFIG + "=tu_uri_completa");
		}
		return uriArchivo.trim();
	}

	/**
	 * Proporciona acceso a la colección de usuarios de la base de datos.
	 * 
	 * @return Un objeto {@link MongoCollection} que contiene los documentos de los
	 *         usuarios, o {@code null} si la conexión con la base de datos no está
	 *         activa.
	 */
	public static MongoCollection<Document> obtenerColeccionUsuarios() {
		return baseDatos != null ? baseDatos.getCollection("Usuarios") : null;
	}

	/**
	 * Proporciona acceso a la colección de temas de la base de datos.
	 * 
	 * @return Un objeto {@link MongoCollection} que contiene los documentos de los
	 *         temas, o {@code null} si la conexión con la base de datos no está
	 *         activa.
	 */
	public static MongoCollection<Document> obtenerColeccionTemas() {
		return baseDatos != null ? baseDatos.getCollection("Temas") : null;
	}

	/**
	 * Proporciona acceso a la colección de tests disponibles en la base de datos.
	 * 
	 * @return Un objeto {@link MongoCollection} que contiene los documentos de los
	 *         tests, o {@code null} si la conexión con la base de datos no está
	 *         activa.
	 */
	public static MongoCollection<Document> obtenerColeccionTests() {
		return baseDatos != null ? baseDatos.getCollection("Tests") : null;
	}

	/**
	 * Proporciona acceso a la colección de preguntas individuales almacenadas en la
	 * base de datos.
	 * 
	 * @return Un objeto {@link MongoCollection} que contiene los documentos de las
	 *         preguntas, o {@code null} si la conexión con la base de datos no está
	 *         activa.
	 */
	public static MongoCollection<Document> obtenerColeccionPreguntas() {
		return baseDatos != null ? baseDatos.getCollection("Preguntas") : null;
	}

	/**
	 * Cierra de manera ordenada y limpia la conexión activa con el cliente de
	 * MongoDB Atlas, liberando los recursos reservados por los sockets de red.
	 */
	public static void cerrarConexion() {
		if (clienteMongo != null) {
			clienteMongo.close();
			System.out.println("Conexión con MongoDB Atlas cerrada.");
		}
	}

	/**
	 * Proporciona acceso a la colección de opciones del menú principal (test
	 * aleatorio, test de errores) de la base de datos.
	 * 
	 * @return Un objeto {@link MongoCollection} que contiene los documentos de las
	 *         opciones, o {@code null} si la conexión con la base de datos no está
	 *         activa.
	 */
	public static MongoCollection<Document> obtenerColeccionOpciones() {
		return baseDatos != null ? baseDatos.getCollection("Opciones") : null;
	}
}
package app;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import app.constantes.Constantes;
import app.datos.ConexionBD;
import app.datos.RepositorioBD;
import app.estilo.Estilo;
import app.logica.EstadoTest;
import app.logica.LogicaTest;
import app.logica.ModoTest;
import app.modelo.Pregunta;
import app.modelo.Tema;
import app.modelo.Test;
import app.modelo.Usuario;
import java.util.HashSet;
import java.util.Set;
import app.modelo.ProgresoTest;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Clase principal que gestiona la interfaz gráfica de usuario (GUI) del sistema
 * de autoescuela. Controla el flujo completo desde el inicio de sesión, el menú
 * principal de práctica, administración de licencias, y la realización
 * interactiva de los tests. No contiene lógica de Mongo (eso vive en
 * {@link RepositorioBD}), reglas de negocio (eso vive en {@link LogicaTest}) ni
 * estilo visual (eso vive en {@link Estilo}): aquí solo se cablean componentes,
 * se reacciona a clics y se navega entre pantallas.
 * 
 * @author Ángel Hernández
 */
public class MainGUI {

	private static DateTimeFormatter formateador = DateTimeFormatter.ofPattern(Constantes.FORMATO_FECHA);

	private static JFrame ventana = new JFrame("Ahevia");
	private static List<Tema> listaTemas;

	// PANELES DE NAVEGACIÓN
	private static JPanel panelLogin = new JPanel();
	private static JPanel panelAdmin = new JPanel();
	private static JPanel panelTema = new JPanel();
	private static JPanel panelTest = new JPanel();
	private static JPanel panelPreguntas = new JPanel();
	private static JPanel panelResultados = new JPanel();

	// COMPONENTES LOGIN
	private static JLabel etiquetaUsuario = new JLabel("Documento de identificación:");
	private static JTextField textFieldUsuario = new JTextField(Constantes.LONGITUD_DNI);
	private static JButton botonIniciarSesion = new JButton("Iniciar sesión");
	private static JLabel mensajeErrorLogin = new JLabel("");
	private static JButton botonContacto = new JButton("Contacto con soporte");

	// COMPONENTES MENÚ PRINCIPAL (paso previo tras el login del alumno)
	private static JPanel panelMenuPrincipal = new JPanel();
	private static JLabel etiquetaMenuPrincipal = new JLabel(Constantes.TITULO_MENU_PRINCIPAL);
	private static JButton botonOpcionTemas = new JButton(Constantes.TEXTO_OPCION_TEMAS);
	private static JButton botonOpcionAleatorios = new JButton("Tests aleatorios");
	private static JButton botonOpcionErrores = new JButton(Constantes.TITULO_TEMA_ERRORES);
	private static JButton botonVolverMenuPrincipal = new JButton("Volver a inicio");

	// COMPONENTES ADMIN
	private static JLabel etiquetaRenovacion = new JLabel("Identificación del alumno:");
	private static JTextField textFieldUsuarioRenovar = new JTextField(Constantes.LONGITUD_DNI);
	private static JButton botonRenovarLicencia = new JButton("Ampliar licencia");
	private static JButton botonVolverInicioAdmin = new JButton("Volver a inicio");
	private static JLabel mensajeErrorAdmin = new JLabel("");
	private static JLabel mensajeConfirmacionAdmin = new JLabel("");

	private static String codigoAdminLogueado = "";
	private static String dniAlumnoLogueado = "";
	private static Usuario usuarioActual;
	private static Tema temaSeleccionadoActual;
	private static Test testEnCurso;
	private static Map<String, String> origenPreguntaErrores;
	private static boolean modoTestErrores = false;
	private static ModoTest modoActual = ModoTest.ESTUDIO;

	private static JPanel panelFinExamen = new JPanel();
	private static JLabel etiquetaNotaFinalExamen = new JLabel("");
	private static JLabel etiquetaVeredictoFinalExamen = new JLabel("");
	private static JButton botonVerCorreccion = new JButton("Ver corrección");

	// COMPONENTES TEMAS
	private static JLabel etiquetaTema = new JLabel("Elige un tema para empezar a practicar");
	private static JButton botonVolverInicioTema = new JButton("Volver a opciones");
	// Referencia al contenedor donde viven los botones de Tema generados
	// dinámicamente, guardada para poder reestilizarlos en el listener de resize
	// (ver reescalarComponentesEstaticos()): sin esto, quedarían fijados con el
	// factorEscala que hubiera en el instante de arrancar la app.
	@SuppressWarnings("unused")
	private static JPanel contenedorTemasPanel;

	// COMPONENTES SELECCIÓN TESTS
	private static JLabel etiquetaSeleccionTest = new JLabel("");
	private static JButton botonVolverTemas = new JButton("Volver a temas");

	// COMPONENTES DINÁMICOS DEL TEST INICIADO
	private static JLabel etiquetaCronometro = new JLabel("Tiempo: 30:00");
	private static JTextPane paneEnunciado = new JTextPane();
	private static JTextPane etiquetaExplicacion = new JTextPane();
	private static JPanel panelBotonesNumeros = new JPanel();
	private static JPanel panelOpcionesRespuesta = new JPanel();
	private static JButton botonFinalizarTest = new JButton("Finalizar test");

	// COMPONENTES PANTALLA RESULTADOS
	private static JLabel etiquetaNotaFinal = new JLabel("");
	private static JLabel etiquetaVeredictoFinal = new JLabel("");
	private static JButton botonSalirResultados = new JButton("Volver a tests");
	private static JButton botonPreguntaAnterior = new JButton();
	private static JButton botonPreguntaSiguiente = new JButton();

	// VARIABLES DE CONTROL DEL TEST ACTUAL
	private static List<Pregunta> preguntasTestActual;
	private static int indicePreguntaActual = 0;
	private static int paginaBotoneraInicio = 0;
	private static int[] respuestasUsuario;
	private static Timer timerCronometro;
	private static int segundosRestantes = Constantes.DURACION_TEST_SEGUNDOS;

	/** Clase de solo componentes estáticos: no debe instanciarse. */
	private MainGUI() {
	}

	/**
	 * Punto de entrada principal de la aplicación. Arranca la interfaz gráfica
	 * asegurando su ejecución en el Event Dispatch Thread (EDT) de Swing. La
	 * conexión a la base de datos la inicializa {@link ConexionBD} de forma
	 * automática (bloque estático) en cuanto se usa por primera vez.
	 * 
	 * @param args Argumentos de línea de comandos (no utilizados).
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			Rectangle limitePantalla = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
			Estilo.inicializarEscala(limitePantalla.width, limitePantalla.height);

			configurarVentana();

			inicioDeSesion();
			configurarMenuPrincipal();
			renovarUsuario();
			seleccionarTema();
			Estilo.aplicarDisenoPanelPreguntas(panelPreguntas, etiquetaCronometro, paneEnunciado,
					panelOpcionesRespuesta, etiquetaExplicacion, panelBotonesNumeros, botonFinalizarTest,
					botonPreguntaAnterior, botonPreguntaSiguiente);
			Estilo.aplicarDisenoPanelResultados(panelResultados, etiquetaNotaFinal, etiquetaVeredictoFinal,
					botonSalirResultados);
			Estilo.aplicarDisenoPanelFinExamen(panelFinExamen, etiquetaNotaFinalExamen, etiquetaVeredictoFinalExamen,
					botonVerCorreccion);
			configurarBotonesFinExamen();

			botonesVolverGlobales();
			configurarAtajoEscape();
			configurarBotonFinalizar();
			configurarBotonesNavegacionPregunta();
			configurarClicEnunciadoImagen();

			limitarLongitud(textFieldUsuarioRenovar, Constantes.LONGITUD_DNI);
			limitarLongitud(textFieldUsuario, Constantes.LONGITUD_DNI);

			ventana.addComponentListener(new java.awt.event.ComponentAdapter() {
				@Override
				public void componentResized(java.awt.event.ComponentEvent e) {
					Estilo.inicializarEscala(ventana.getWidth(), ventana.getHeight());

					reescalarComponentesEstaticos();

					ventana.revalidate();
					ventana.repaint();
				}
			});

			ventana.setVisible(true);
			textFieldUsuario.requestFocusInWindow();
		});
	}

	/**
	 * Configura el panel del menú principal, mostrado tras el login del alumno como
	 * paso previo antes de elegir cómo quiere practicar. Vincula sus tres rutas:
	 * "Tests por temas" (va directo al panel de Temas ya existente), "Test
	 * aleatorio" y "Test con todos los errores" (la primera carga su Tema desde la
	 * colección "Opciones" y reutiliza el mismo grid de Tests que un tema normal;
	 * la segunda arranca directamente el motor del examen con las preguntas
	 * falladas del alumno).
	 */
	private static void configurarMenuPrincipal() {
		Estilo.aplicarDisenoPanelMenu(panelMenuPrincipal, etiquetaMenuPrincipal, botonOpcionTemas,
				botonOpcionAleatorios, botonOpcionErrores, botonVolverMenuPrincipal);

		botonOpcionTemas.addActionListener(e -> cambiarPanel(panelMenuPrincipal, panelTema));
		botonOpcionAleatorios.addActionListener(e -> prepararOpcionTest(Constantes.ID_OPCION_ALEATORIOS));
		botonOpcionErrores.addActionListener(e -> prepararTestErrores());

		botonVolverMenuPrincipal.addActionListener(e -> {
			limpiarCamposLogin();
			cambiarPanel(panelMenuPrincipal, panelLogin);
			textFieldUsuario.requestFocusInWindow();
		});
	}

	/**
	 * Carga desde MongoDB (vía {@link RepositorioBD#cargarOpcion}) la opción del
	 * menú principal indicada y navega al grid de Tests ya existente, igual que si
	 * el alumno hubiera entrado a un Tema normal desde el panel de Temas. No hace
	 * nada si la opción no existe en la base de datos.
	 *
	 * @param idOpcion El identificador de la opción elegida (ej.
	 *                 {@link Constantes#ID_OPCION_ALEATORIOS}).
	 */
	private static void prepararOpcionTest(String idOpcion) {
		Tema tema = RepositorioBD.cargarOpcion(idOpcion);
		if (tema == null) {
			return;
		}
		seleccionarTest(tema);
		cambiarPanel(panelMenuPrincipal, panelTest);
	}

	/**
	 * Reaplica el estilo (fuente, tamaño, márgenes...) a TODOS los componentes que
	 * se construyeron una única vez en {@code main()} (Login, Menú principal,
	 * Admin, cabecera de Temas, botones de Tema, cabecera de Resultados y el panel
	 * de Preguntas), con el {@code factorEscala} recién recalculado.
	 * 
	 * <p>
	 * FIX: antes solo se reestilizaban aquí el enunciado, la explicación y la
	 * botonera numérica del panel de Preguntas. El resto de pantallas se construyen
	 * una sola vez al arrancar la app (antes del primer resize real, p. ej. al
	 * maximizar la ventana), así que se quedaban fijadas con el
	 * {@code factorEscala} inicial. El grid de Tests, en cambio, se reconstruye
	 * cada vez que el alumno entra en un Tema ({@link #construirGridTests}), así
	 * que siempre reflejaba el {@code factorEscala} más reciente. Llamar a estos
	 * métodos de nuevo aquí no reintroduce ningún listener duplicado: son puros
	 * "configuradores" de estilo, no registran eventos.
	 */
	private static void reescalarComponentesEstaticos() {
		Estilo.aplicarDisenoPanelLogin(panelLogin, etiquetaUsuario, textFieldUsuario, botonIniciarSesion,
				mensajeErrorLogin, botonContacto);

		Estilo.aplicarDisenoPanelMenu(panelMenuPrincipal, etiquetaMenuPrincipal, botonOpcionTemas,
				botonOpcionAleatorios, botonOpcionErrores, botonVolverMenuPrincipal);
		actualizarBotonOpcionErrores();

		Estilo.aplicarDisenoPanelAdmin(panelAdmin, etiquetaRenovacion, textFieldUsuarioRenovar, botonRenovarLicencia,
				mensajeErrorAdmin, mensajeConfirmacionAdmin, botonVolverInicioAdmin);

		JPanel contenedorTemas = Estilo.aplicarDisenoPanelTemas(panelTema, etiquetaTema, botonVolverInicioTema);
		contenedorTemasPanel = contenedorTemas;
		poblarBotonesTemas(contenedorTemas);

		if (temaSeleccionadoActual != null) {
			construirGridTests(temaSeleccionadoActual);
		}

		Estilo.aplicarDisenoPanelPreguntas(panelPreguntas, etiquetaCronometro, paneEnunciado, panelOpcionesRespuesta,
				etiquetaExplicacion, panelBotonesNumeros, botonFinalizarTest, botonPreguntaAnterior,
				botonPreguntaSiguiente);

		Estilo.aplicarDisenoPanelResultados(panelResultados, etiquetaNotaFinal, etiquetaVeredictoFinal,
				botonSalirResultados);
		Estilo.aplicarDisenoPanelFinExamen(panelFinExamen, etiquetaNotaFinalExamen, etiquetaVeredictoFinalExamen,
				botonVerCorreccion);
	}

	/**
	 * Restringe el número de caracteres que un usuario puede introducir en un campo
	 * de texto. Se utiliza principalmente para asegurar que el DNI no supere la
	 * longitud estipulada.
	 * 
	 * @param campo          El JTextField al que se aplicará la restricción.
	 * @param longitudMaxima El número máximo de caracteres permitidos.
	 */
	private static void limitarLongitud(JTextField campo, int longitudMaxima) {
		campo.setDocument(new PlainDocument() {
			@Override
			public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
				if (str == null) {
					return;
				}
				if ((getLength() + str.length()) <= longitudMaxima) {
					super.insertString(offs, str, a);
				}
			}
		});
	}

	/**
	 * Asigna los eventos de clic a los botones de retroceso ("Volver") de la
	 * aplicación. Garantiza que la navegación entre los distintos paneles sea
	 * fluida y limpie los campos residuales al regresar a las pantallas
	 * principales.
	 */
	private static void botonesVolverGlobales() {
		botonVolverTemas.addActionListener(ex -> {
			boolean esOpcionMenu = temaSeleccionadoActual != null
					&& (Constantes.ID_OPCION_ALEATORIOS.equals(temaSeleccionadoActual.getId())
							|| Constantes.ID_OPCION_ERRORES.equals(temaSeleccionadoActual.getId()));
			cambiarPanel(panelTest, esOpcionMenu ? panelMenuPrincipal : panelTema);
		});

		botonVolverInicioTema.addActionListener(ex -> cambiarPanel(panelTema, panelMenuPrincipal));

		botonVolverInicioAdmin.addActionListener(ex -> {
			limpiarCamposLogin();
			limpiarCamposAdmin();
			cambiarPanel(panelAdmin, panelLogin);
			textFieldUsuario.requestFocusInWindow();
		});

		botonSalirResultados.addActionListener(ex -> {
			if (modoTestErrores) {
				modoTestErrores = false;
				cambiarPanel(panelResultados, panelMenuPrincipal);
			} else {
				construirGridTests(temaSeleccionadoActual);
				cambiarPanel(panelResultados, panelTest);
			}
		});
	}

	/**
	 * Limpia el campo de usuario y el mensaje de error del panel de login,
	 * dejándolo listo para un nuevo intento. Centraliza un reseteo que antes estaba
	 * repetido en dos listeners distintos.
	 */
	private static void limpiarCamposLogin() {
		textFieldUsuario.setText("");
		mensajeErrorLogin.setText("");
	}

	/**
	 * Abre en el navegador predeterminado del sistema el enlace de contacto/
	 * soporte configurado en {@link Constantes#URL_CONTACTO}, asociado al botón
	 * "Contáctenos" del panel de Login.
	 */
	private static void abrirEnlaceContacto() {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().browse(new URI(Constantes.URL_CONTACTO));
			}
		} catch (Exception ex) {
			System.err.println("Error al abrir el enlace de contacto: " + ex.getMessage());
		}
	}

	/**
	 * Limpia el campo de usuario a renovar y los mensajes de aviso del panel de
	 * administración, dejándolo listo para una nueva operación.
	 */
	private static void limpiarCamposAdmin() {
		textFieldUsuarioRenovar.setText("");
		mensajeErrorAdmin.setText("");
		mensajeConfirmacionAdmin.setText("");
	}

	/**
	 * Configura el panel inicial de autenticación. Valida el formato del DNI (vía
	 * {@link LogicaTest}), comprueba su existencia en la base de datos (vía
	 * {@link RepositorioBD}) y redirige al panel de Administración o al Menú
	 * principal según el rol y la caducidad de la licencia.
	 */
	private static void inicioDeSesion() {
		Estilo.aplicarDisenoPanelLogin(panelLogin, etiquetaUsuario, textFieldUsuario, botonIniciarSesion,
				mensajeErrorLogin, botonContacto);

		ventana.add(panelLogin);

		textFieldUsuario.addActionListener(e -> botonIniciarSesion.doClick());
		botonContacto.addActionListener(e -> abrirEnlaceContacto());

		botonIniciarSesion.addActionListener(e -> {
			String dniIntroducido = LogicaTest.normalizarDni(textFieldUsuario.getText());
			mensajeErrorLogin.setText("");

			if (dniIntroducido.isEmpty()) {
				mensajeErrorLogin.setText("No puede dejar este espacio en blanco.");
				refrescarPanel(panelLogin);
				return;
			}

			Usuario usuarioBD = RepositorioBD.obtenerUsuario(dniIntroducido);

			if (!RepositorioBD.hayConexion()) {
				mensajeErrorLogin.setText("Error de conexión: No se pudo contactar con el servidor.");
			} else if (usuarioBD != null && usuarioBD.isAdministrador()) {
				codigoAdminLogueado = dniIntroducido;
				cambiarPanel(panelLogin, panelAdmin);
				textFieldUsuarioRenovar.requestFocusInWindow();
				return;
			} else if (!LogicaTest.dniValido(dniIntroducido)) {
				mensajeErrorLogin.setText("El documento de identificación no es válido.");
			} else if (usuarioBD == null) {
				mensajeErrorLogin.setText("No existe ningún usuario con este documento.");
			} else if (!usuarioBD.isLicenciaActiva()) {
				mensajeErrorLogin.setText("Su licencia ha caducado.");
			} else {
				dniAlumnoLogueado = dniIntroducido;
				usuarioActual = usuarioBD;
				actualizarBotonOpcionErrores();
				cambiarPanel(panelLogin, panelMenuPrincipal);
				return;
			}
			refrescarPanel(panelLogin);
		});
	}

	/**
	 * Configura el panel del Administrador. Permite la creación de nuevos usuarios
	 * o la renovación de licencias existentes, delegando en
	 * {@link RepositorioBD#renovarOcrearLicencia} la suma de 30 días a la fecha de
	 * caducidad y el registro de la operación en la BD.
	 */
	private static void renovarUsuario() {
		Estilo.aplicarDisenoPanelAdmin(panelAdmin, etiquetaRenovacion, textFieldUsuarioRenovar, botonRenovarLicencia,
				mensajeErrorAdmin, mensajeConfirmacionAdmin, botonVolverInicioAdmin);

		textFieldUsuarioRenovar.addActionListener(e -> botonRenovarLicencia.doClick());

		botonRenovarLicencia.addActionListener(e -> {
			String dniIntroducido = LogicaTest.normalizarDni(textFieldUsuarioRenovar.getText());
			mensajeErrorAdmin.setText("");
			mensajeConfirmacionAdmin.setText("");

			if (dniIntroducido.isEmpty()) {
				mensajeErrorAdmin.setText("No puede dejar este espacio en blanco.");
			} else if (!RepositorioBD.hayConexion()) {
				mensajeErrorAdmin.setText("Error de conexión: No se pudo contactar con el servidor.");
			} else if (!LogicaTest.dniValido(dniIntroducido)) {
				mensajeErrorAdmin.setText("El documento de identificación no es válido.");
			} else {
				try {
					LocalDateTime resultado = RepositorioBD.renovarOcrearLicencia(dniIntroducido, codigoAdminLogueado);
					String fechaTexto = resultado.toLocalDate().format(formateador);

					mensajeConfirmacionAdmin.setText("Licencia ampliada con éxito hasta: " + fechaTexto);
				} catch (Exception ex) {
					System.err.println("Error al renovar/crear usuario: " + ex.getMessage());
					mensajeErrorAdmin.setText("Error al guardar los datos. Inténtelo de nuevo.");
				}
			}
			refrescarPanel(panelAdmin);
		});
	}

	/**
	 * Renderiza el panel de Temas, extrayendo los temas (vía
	 * {@link RepositorioBD#cargarTemas()}) y generando un botón dinámico por cada
	 * tema disponible. Los Tests de cada tema todavía no se cargan aquí: se piden a
	 * demanda en {@link #seleccionarTest(Tema)} (carga perezosa).
	 */
	private static void seleccionarTema() {
		listaTemas = RepositorioBD.cargarTemas();
		JPanel contenedorTemas = Estilo.aplicarDisenoPanelTemas(panelTema, etiquetaTema, botonVolverInicioTema);
		contenedorTemasPanel = contenedorTemas;

		poblarBotonesTemas(contenedorTemas);
	}

	/**
	 * Genera un botón por cada Tema ya cargado en memoria ({@code listaTemas}) y
	 * los añade al contenedor indicado. Se extrajo de {@link #seleccionarTema()}
	 * para poder repoblar el mismo contenedor tras reconstruir su tarjeta en
	 * {@link #reescalarComponentesEstaticos()} (al redimensionar la ventana), sin
	 * tener que volver a consultar la base de datos cada vez.
	 *
	 * @param contenedorTemas El panel (ya vacío) donde añadir los botones.
	 */
	private static void poblarBotonesTemas(JPanel contenedorTemas) {
		contenedorTemas.removeAll();
		for (Tema temaActual : listaTemas) {
			JButton botonTema = new JButton(temaActual.getTitulo());
			botonTema.addActionListener(e -> {
				seleccionarTest(temaActual);
				cambiarPanel(panelTema, panelTest);
			});
			Estilo.configurarBotonTema(botonTema, contenedorTemas, Constantes.ANCHO_BOTON_TEMA);
		}
	}

	/**
	 * Fija el Tema (o la opción de menú, ya que ambas comparten el mismo modelo
	 * {@link Tema}) activo y construye el grid de Tests correspondiente. Ya no
	 * decide la navegación: eso lo hace cada llamador según de dónde viene (panel
	 * de Temas o Menú principal).
	 *
	 * @param temaActual El Tema/opción elegido por el alumno.
	 */
	private static void seleccionarTest(Tema temaActual) {
		temaSeleccionadoActual = temaActual;
		etiquetaSeleccionTest.setText("Tests disponibles para " + "\"" + temaActual.getTitulo() + "\"");
		construirGridTests(temaActual);
	}

	/**
	 * Reconstruye desde cero el grid de botones de Tests de un Tema, coloreando
	 * cada uno según el progreso guardado del alumno (vía
	 * {@link LogicaTest#clasificarTest}). Se llama tanto al entrar al Tema como al
	 * volver desde Resultados, para reflejar el progreso actualizado.
	 *
	 * @param temaActual El Tema cuyos Tests se van a listar.
	 */
	private static void construirGridTests(Tema temaActual) {
		JPanel contenedorGrid = Estilo.aplicarDisenoPanelTests(panelTest, etiquetaSeleccionTest, botonVolverTemas);

		List<Test> tests = RepositorioBD.obtenerTestsDeTema(temaActual);
		int totalTests = tests.size();

		contenedorGrid.removeAll();
		if (totalTests > 0) {
			Estilo.configurarGridTests(contenedorGrid);

			int contador = 1;
			for (Test testActual : tests) {
				JButton botonTest = new JButton(String.valueOf(contador));
				botonTest.addActionListener(ex -> {
					ProgresoTest ptDialogo = usuarioActual != null ? usuarioActual.getProgreso().get(testActual.getId())
							: null;
					boolean repasoDisponible = ptDialogo != null && !ptDialogo.getRespuestasUltimoIntento().isEmpty();

					Estilo.mostrarDialogoSeleccionModo(ventana, repasoDisponible,
							() -> prepararEIniciarTest(testActual, ModoTest.ESTUDIO),
							() -> prepararEIniciarTest(testActual, ModoTest.EXAMEN), () -> prepararRepaso(testActual));
				});
				Estilo.configurarBotonCuadradoTest(botonTest);

				ProgresoTest pt = usuarioActual != null ? usuarioActual.getProgreso().get(testActual.getId()) : null;
				EstadoTest estado = LogicaTest.clasificarTest(pt != null ? pt.getFallos() : null);
				Estilo.colorearSegunProgreso(botonTest, estado);

				contenedorGrid.add(botonTest);
				contador += 1;
			}

			refrescarPanel(contenedorGrid);
		}
	}

	// ==========================================
	// MOTOR DEL EXAMEN
	// ==========================================

	/**
	 * Carga las preguntas del Test elegido (vía
	 * {@link RepositorioBD#obtenerPreguntasDeTest}) y, si tiene contenido, arranca
	 * el motor del examen en modo normal y navega al panel de Preguntas.
	 *
	 * @param testActual El Test pulsado por el alumno en el grid.
	 * @param modo       El modo de test elegido (Estudio o Examen) en el diálogo de
	 *                   selección.
	 */
	private static void prepararEIniciarTest(Test testActual, ModoTest modo) {
		preguntasTestActual = RepositorioBD.obtenerPreguntasDeTest(testActual);
		if (preguntasTestActual.isEmpty()) {
			return;
		}
		testEnCurso = testActual;
		modoTestErrores = false;
		modoActual = modo;
		arrancarMotorExamen();
		cambiarPanel(panelTest, panelPreguntas);
	}

	/**
	 * Arranca el modo Repaso de un Test concreto: reconstruye "respuestasUsuario" a
	 * partir de {@code RespuestasUltimoIntento} guardado en el último intento en
	 * modo Examen, en el mismo orden que "preguntasTestActual" (mismos ids que se
	 * usaron para guardar). No hace nada si no hay un intento previo guardado.
	 * 
	 * @param testActual El Test cuyo último intento en modo Examen se quiere
	 *                   repasar.
	 */
	private static void prepararRepaso(Test testActual) {
		ProgresoTest pt = usuarioActual != null ? usuarioActual.getProgreso().get(testActual.getId()) : null;
		if (pt == null || pt.getRespuestasUltimoIntento().isEmpty()) {
			return;
		}
		preguntasTestActual = RepositorioBD.obtenerPreguntasDeTest(testActual);
		if (preguntasTestActual.isEmpty()) {
			return;
		}
		testEnCurso = testActual;
		modoTestErrores = false;
		modoActual = ModoTest.REPASO;

		respuestasUsuario = new int[preguntasTestActual.size()];
		Map<String, Integer> guardadas = pt.getRespuestasUltimoIntento();
		for (int i = 0; i < preguntasTestActual.size(); i++) {
			Integer respuesta = guardadas.get(preguntasTestActual.get(i).getId());
			respuestasUsuario[i] = respuesta != null ? respuesta : -1;
		}

		indicePreguntaActual = 0;
		paginaBotoneraInicio = 0;
		if (timerCronometro != null && timerCronometro.isRunning()) {
			timerCronometro.stop();
		}
		etiquetaCronometro.setText(Constantes.TEXTO_CRONOMETRO_REPASO);
		actualizarTextoBotonFinalizar();
		generarBotoneraNumerica();
		mostrarPreguntaEspecifica(0);
		cambiarPanel(panelTest, panelPreguntas);
	}

	/**
	 * Recopila, a partir del progreso de todos los Tests del alumno, las preguntas
	 * falladas (guardando su test de origen para
	 * {@link #guardarCorreccionesErrores()}) y arranca con ellas el motor del
	 * examen en modo "Test de errores". Navega desde el Menú principal.
	 */
	private static void prepararTestErrores() {
		Map<String, String> origen = new HashMap<>();
		Set<String> idsFallidas = new LinkedHashSet<>();
		if (usuarioActual != null) {
			for (Map.Entry<String, ProgresoTest> entrada : usuarioActual.getProgreso().entrySet()) {
				for (String idPregunta : entrada.getValue().getIdsPreguntasFalladas()) {
					idsFallidas.add(idPregunta);
					origen.put(idPregunta, entrada.getKey());
				}
			}
		}
		if (idsFallidas.isEmpty()) {
			return;
		}
		preguntasTestActual = RepositorioBD.obtenerPreguntasPorIds(new ArrayList<>(idsFallidas));
		if (preguntasTestActual.isEmpty()) {
			return;
		}
		origenPreguntaErrores = origen;
		testEnCurso = null;
		modoTestErrores = true;
		modoActual = ModoTest.ESTUDIO;
		arrancarMotorExamen();
		cambiarPanel(panelMenuPrincipal, panelPreguntas);
	}

	/**
	 * Comprueba si el alumno actual tiene alguna pregunta fallada pendiente en
	 * cualquiera de sus Tests. Decide si el botón de "Test de errores" debe estar
	 * habilitado.
	 *
	 * @return {@code true} si existe al menos una pregunta fallada sin corregir.
	 */
	private static boolean hayPreguntasFalladas() {
		if (usuarioActual == null) {
			return false;
		}
		for (ProgresoTest pt : usuarioActual.getProgreso().values()) {
			if (!pt.getIdsPreguntasFalladas().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Sincroniza el habilitado/deshabilitado del botón "Test de errores" del Menú
	 * principal con {@link #hayPreguntasFalladas()}.
	 */
	private static void actualizarBotonOpcionErrores() {
		botonOpcionErrores.setEnabled(hayPreguntasFalladas());
	}

	/**
	 * Reinicia el motor del examen para un Test (o repaso de errores) nuevo: limpia
	 * respuestas previas, arranca o desactiva el cronómetro según
	 * {@code modoTestErrores} y muestra la primera pregunta.
	 */
	private static void arrancarMotorExamen() {
		indicePreguntaActual = 0;
		paginaBotoneraInicio = 0;
		actualizarTextoBotonFinalizar();

		respuestasUsuario = new int[preguntasTestActual.size()];
		Arrays.fill(respuestasUsuario, -1);

		if (timerCronometro != null && timerCronometro.isRunning()) {
			timerCronometro.stop();
		}

		if (modoTestErrores) {
			etiquetaCronometro.setText(Constantes.TEXTO_CRONOMETRO_ERRORES);
		} else if (modoActual == ModoTest.EXAMEN) {
			segundosRestantes = Constantes.DURACION_TEST_SEGUNDOS;
			etiquetaCronometro.setText(formatearTiempoRestante());

			timerCronometro = new Timer(1000, e -> {
				segundosRestantes--;
				etiquetaCronometro.setText(formatearTiempoRestante());

				if (segundosRestantes <= 0) {
					timerCronometro.stop();
					calcularNotaYMostrarResultados();
				}
			});

			timerCronometro.start();
		} else {
			etiquetaCronometro.setText(Constantes.TEXTO_CRONOMETRO_ESTUDIO);
		}

		generarBotoneraNumerica();
		mostrarPreguntaEspecifica(0);
	}

	/**
	 * Formatea el tiempo restante del cronómetro en formato "Tiempo: mm:ss".
	 * Centraliza un formato que antes estaba repetido a mano tanto al arrancar el
	 * test como en cada tick del temporizador.
	 * 
	 * @return El texto ya formateado para {@code etiquetaCronometro}.
	 */
	private static String formatearTiempoRestante() {
		return String.format("Tiempo: %02d:%02d", segundosRestantes / 60, segundosRestantes % 60);
	}

	/**
	 * Vincula una única vez el botón de finalizar test con la lógica de cierre. No
	 * es necesario desligar y volver a ligar este listener cada vez que arranca un
	 * test nuevo: al ser {@code timerCronometro} un campo estático, el listener
	 * siempre consulta su valor vigente en el momento del clic, no el que existía
	 * cuando se registró.
	 * 
	 * <p>
	 * FIX: se añade una comprobación de {@code null} antes de detener el
	 * cronómetro. En el flujo normal el botón solo es visible una vez arrancado un
	 * test (y por tanto {@code timerCronometro} ya existe), pero el guard es
	 * gratuito y evita un {@code NullPointerException} si la navegación cambia en
	 * el futuro.
	 */
	private static void configurarBotonFinalizar() {
		botonFinalizarTest.addActionListener(e -> {
			if (timerCronometro != null) {
				timerCronometro.stop();
			}
			if (modoActual == ModoTest.REPASO) {
				construirGridTests(temaSeleccionadoActual);
				cambiarPanel(panelPreguntas, panelTest);
			} else if (modoActual == ModoTest.EXAMEN) {
				finalizarExamen();
			} else {
				calcularNotaYMostrarResultados();
			}
		});
	}

	/**
	 * Vincula una única vez los botones de la pantalla intermedia de fin de Examen:
	 * "Volver a tests" descarta la corrección y vuelve al grid; "Ver corrección"
	 * pasa a modo Repaso con las respuestas recién dadas, sin necesidad de releer
	 * de MongoDB.
	 */
	private static void configurarBotonesFinExamen() {
		botonVerCorreccion.addActionListener(e -> {
			modoActual = ModoTest.REPASO;
			indicePreguntaActual = 0;
			paginaBotoneraInicio = 0;
			etiquetaCronometro.setText(Constantes.TEXTO_CRONOMETRO_REPASO);
			actualizarTextoBotonFinalizar();
			generarBotoneraNumerica();
			mostrarPreguntaEspecifica(0);
			cambiarPanel(panelFinExamen, panelPreguntas);
		});
	}

	/**
	 * Corrige y guarda el intento de Examen (fallos, preguntas falladas para el
	 * modo "Test de errores" y {@code RespuestasUltimoIntento} para habilitar
	 * Repaso), y navega a la pantalla intermedia de fin de Examen en vez de ir
	 * directo a Resultados.
	 */
	private static void finalizarExamen() {
		int resultado = LogicaTest.calcularResultado(preguntasTestActual, respuestasUsuario);
		boolean aprobado = LogicaTest.apto(resultado);

		etiquetaNotaFinalExamen.setText("Has fallado " + resultado + " preguntas.");
		etiquetaVeredictoFinalExamen.setText(aprobado ? "APTO" : "NO APTO");
		etiquetaVeredictoFinalExamen.setForeground(aprobado ? Constantes.COLOR_APTO : Constantes.COLOR_NO_APTO);

		if (usuarioActual != null && testEnCurso != null) {
			ProgresoTest anterior = usuarioActual.getProgreso().get(testEnCurso.getId());
			Set<String> previas = anterior != null ? anterior.getIdsPreguntasFalladas() : new HashSet<>();
			Set<String> actualizadas = LogicaTest.actualizarPreguntasFalladas(previas, preguntasTestActual,
					respuestasUsuario);

			Map<String, Integer> respuestasPorId = new HashMap<>();
			for (int i = 0; i < preguntasTestActual.size(); i++) {
				respuestasPorId.put(preguntasTestActual.get(i).getId(), respuestasUsuario[i]);
			}

			usuarioActual.actualizarProgreso(testEnCurso.getId(), resultado, actualizadas, respuestasPorId);
			RepositorioBD.guardarProgresoTest(dniAlumnoLogueado, testEnCurso.getId(), resultado, actualizadas,
					respuestasPorId);
		}

		actualizarBotonOpcionErrores();
		refrescarPanel(panelFinExamen);
		cambiarPanel(panelPreguntas, panelFinExamen);
	}

	/**
	 * Vincula una única vez las flechas Anterior/Siguiente para mover el índice de
	 * pregunta actual, respetando los límites de {@code preguntasTestActual}.
	 */
	private static void configurarBotonesNavegacionPregunta() {
		botonPreguntaAnterior.addActionListener(e -> {
			if (indicePreguntaActual > 0) {
				mostrarPreguntaEspecifica(indicePreguntaActual - 1);
			}
		});
		botonPreguntaSiguiente.addActionListener(e -> {
			if (indicePreguntaActual < preguntasTestActual.size() - 1) {
				mostrarPreguntaEspecifica(indicePreguntaActual + 1);
			}
		});
	}

	/**
	 * Vincula una única vez el clic sobre el enunciado de la pregunta: si la
	 * pregunta actual tiene imagen asociada, muestra dicha imagen. Su aspecto
	 * (resaltado en azul, cursor de mano) se sincroniza en
	 * {@link #mostrarPreguntaEspecifica} según {@link Pregunta#isTieneImagen()};
	 * aquí solo se vincula el clic, una única vez, igual que antes se hacía con el
	 * botón de texto "Ver imagen".
	 */
	private static void configurarClicEnunciadoImagen() {
		paneEnunciado.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (preguntasTestActual != null && preguntasTestActual.get(indicePreguntaActual).isTieneImagen()) {
					mostrarImagenPregunta();
				}
			}
		});
	}

	/**
	 * Muestra en una ventana emergente (cerrable) la imagen asociada a la pregunta
	 * actual, buscándola en {@link Constantes#URL_BASE_IMAGENES} con el mismo
	 * nombre que el id de la pregunta. No hace nada si no se encuentra la imagen.
	 */
	private static void mostrarImagenPregunta() {
		Pregunta pregunta = preguntasTestActual.get(indicePreguntaActual);
		try {
			URL urlImagen = new URI(Constantes.URL_BASE_IMAGENES + pregunta.getId() + ".jpg").toURL();
			Image imagenOriginal = new ImageIcon(urlImagen).getImage();
			Image imagenEscalada = imagenOriginal.getScaledInstance(Constantes.ANCHO_DIALOGO_IMAGEN, -1,
					Image.SCALE_SMOOTH);
			javax.swing.JOptionPane.showMessageDialog(ventana, new JLabel(new ImageIcon(imagenEscalada)),
					"Imagen de la pregunta", javax.swing.JOptionPane.PLAIN_MESSAGE);
		} catch (Exception ex) {
			System.err.println("Error al cargar la imagen de la pregunta " + pregunta.getId() + ": " + ex.getMessage());
		}
	}

	/**
	 * Repinta por completo la botonera numérica. Con
	 * {@link Constantes#HUECOS_BOTONERA} o menos preguntas se listan todas de una
	 * vez (comportamiento original, sin paginar). Con más, se pagina: se muestra el
	 * tramo de preguntas que quepa en los huecos libres (tras reservar, si hace
	 * falta, un hueco para la flecha "anterior" a la izquierda y/o "siguiente" a la
	 * derecha), calculado por {@link #calcularRangoPagina}.
	 */
	private static void generarBotoneraNumerica() {
		panelBotonesNumeros.removeAll();
		int total = preguntasTestActual.size();

		if (total <= Constantes.HUECOS_BOTONERA) {
			for (int i = 0; i < total; i++) {
				panelBotonesNumeros.add(crearBotonNumero(i));
			}
		} else {
			int[] rangoPagina = calcularRangoPagina(paginaBotoneraInicio, total);
			boolean hayAnterior = rangoPagina[2] == 1;
			boolean haySiguiente = rangoPagina[3] == 1;

			if (hayAnterior) {
				panelBotonesNumeros.add(crearFlechaPaginacion(false, total));
			}
			for (int i = rangoPagina[0]; i < rangoPagina[1]; i++) {
				panelBotonesNumeros.add(crearBotonNumero(i));
			}
			if (haySiguiente) {
				panelBotonesNumeros.add(crearFlechaPaginacion(true, total));
			}
		}

		refrescarPanel(panelPreguntas);
	}

	/**
	 * Construye un botón numérico individual de la botonera (extraído de
	 * {@link #generarBotoneraNumerica} para poder reutilizarlo tanto en el modo sin
	 * paginar como en cada página).
	 *
	 * @param posicion Índice (0-based) de la pregunta dentro de
	 *                 "preguntasTestActual".
	 */
	private static JButton crearBotonNumero(int posicion) {
		JButton botonNum = new JButton(String.valueOf(posicion + 1));

		botonNum.addActionListener(e -> mostrarPreguntaEspecifica(posicion));
		Estilo.configurarBotonCuadradoTest(botonNum);

		if (posicion == indicePreguntaActual) {
			botonNum.setSelected(true);
		}

		int respuestaElegida = respuestasUsuario[posicion];
		if (modoActual == ModoTest.REPASO) {
			Pregunta preg = preguntasTestActual.get(posicion);
			boolean acierto = respuestaElegida != -1 && respuestaElegida == preg.getIndiceRespuestaCorrecta();
			Estilo.colorearSegunAcierto(botonNum, acierto);
		} else if (respuestaElegida != -1) {
			if (modoActual == ModoTest.EXAMEN) {
				Estilo.colorearComoMarcadaExamen(botonNum);
			} else {
				Pregunta preg = preguntasTestActual.get(posicion);
				boolean acierto = respuestaElegida == preg.getIndiceRespuestaCorrecta();
				Estilo.colorearSegunAcierto(botonNum, acierto);
			}
		}

		return botonNum;
	}

	/**
	 * Construye la flecha de paginación (reutilizando el mismo triángulo dibujado a
	 * mano de {@link Estilo#configurarBotonFlecha} que ya usan las flechas
	 * Anterior/Siguiente pregunta) y le vincula el cambio de página.
	 *
	 * @param avanza {@code true} para la flecha "siguiente página" (a la derecha),
	 *               {@code false} para "página anterior" (a la izquierda).
	 * @param total  Número total de preguntas del test actual.
	 */
	private static JButton crearFlechaPaginacion(boolean avanza, int total) {
		JButton flecha = new JButton();
		Estilo.configurarBotonFlecha(flecha, avanza);

		if (avanza) {
			flecha.addActionListener(e -> {
				int[] rangoActual = calcularRangoPagina(paginaBotoneraInicio, total);
				paginaBotoneraInicio = rangoActual[1];
				generarBotoneraNumerica();
			});
		} else {
			flecha.addActionListener(e -> {
				paginaBotoneraInicio = calcularInicioPaginaAnterior(paginaBotoneraInicio, total);
				generarBotoneraNumerica();
			});
		}

		return flecha;
	}

	/**
	 * Calcula el tramo de preguntas [inicio, fin) que corresponde mostrar en la
	 * página que empieza en "inicio", junto con si esa página necesita flecha
	 * "anterior" (siempre que "inicio" no sea 0) y/o flecha "siguiente" (siempre
	 * que sobren preguntas tras rellenar los huecos disponibles). Reservar cada
	 * flecha resta un hueco de los {@link Constantes#HUECOS_BOTONERA} disponibles
	 * en la fila.
	 *
	 * @param inicio Índice (0-based) de la primera pregunta de esta página.
	 * @param total  Número total de preguntas del test actual.
	 * @return Array {@code [inicio, fin, hayAnterior(0/1), haySiguiente(0/1)]}.
	 */
	private static int[] calcularRangoPagina(int inicio, int total) {
		boolean hayAnterior = inicio > 0;
		int huecosDisponibles = Constantes.HUECOS_BOTONERA - (hayAnterior ? 1 : 0);

		int restantes = total - inicio;
		boolean haySiguiente = restantes > huecosDisponibles;
		if (haySiguiente) {
			huecosDisponibles -= 1;
		}

		int cantidadMostrar = Math.min(huecosDisponibles, restantes);
		return new int[] { inicio, inicio + cantidadMostrar, hayAnterior ? 1 : 0, haySiguiente ? 1 : 0 };
	}

	/**
	 * Recorre las páginas desde el principio hasta encontrar cuál precede a la
	 * página que empieza en "inicioActual", ya que las páginas solo se calculan
	 * hacia adelante (ver {@link #calcularRangoPagina}) y no hay un índice inverso
	 * guardado.
	 */
	private static int calcularInicioPaginaAnterior(int inicioActual, int total) {
		int inicio = 0;
		int inicioAnterior = 0;
		while (inicio < inicioActual) {
			inicioAnterior = inicio;
			inicio = calcularRangoPagina(inicio, total)[1];
		}
		return inicioAnterior;
	}

	/**
	 * Recalcula {@code paginaBotoneraInicio} para que la página visible contenga la
	 * pregunta "indice", recorriendo las páginas desde el principio. Se llama antes
	 * de repintar la botonera en {@link #mostrarPreguntaEspecifica} para que las
	 * flechas Anterior/Siguiente pregunta (que pueden cruzar el límite de una
	 * página) salten automáticamente a la página correcta.
	 */
	private static void ajustarPaginaBotoneraParaIndice(int indice) {
		int total = preguntasTestActual.size();
		if (total <= Constantes.HUECOS_BOTONERA) {
			paginaBotoneraInicio = 0;
			return;
		}
		int inicio = 0;
		while (true) {
			int[] rango = calcularRangoPagina(inicio, total);
			if (indice >= rango[0] && indice < rango[1]) {
				paginaBotoneraInicio = inicio;
				return;
			}
			inicio = rango[1];
		}
	}

	/**
	 * Renderiza la pregunta en "indice": actualiza flechas de navegación, repinta
	 * la botonera numérica y construye los botones de opción (interactivos si no se
	 * ha respondido, bloqueados con su corrección visual si ya se respondió).
	 *
	 * @param indice Posición de la pregunta dentro de "preguntasTestActual".
	 */
	private static void mostrarPreguntaEspecifica(int indice) {
		indicePreguntaActual = indice;
		botonPreguntaAnterior.setEnabled(indice > 0);
		botonPreguntaSiguiente.setEnabled(indice < preguntasTestActual.size() - 1);
		ajustarPaginaBotoneraParaIndice(indice);
		generarBotoneraNumerica();
		Pregunta pregunta = preguntasTestActual.get(indice);

		String textoEnunciado = (indice + 1) + " - " + pregunta.getEnunciado();
		if (pregunta.isTieneImagen()) {
			textoEnunciado += " \uD83D\uDCF7";
		}

		Estilo.establecerTextoCentrado(paneEnunciado, textoEnunciado);
		Estilo.actualizarEstiloEnunciado(paneEnunciado, pregunta.isTieneImagen());
		panelOpcionesRespuesta.removeAll();

		List<String> opciones = pregunta.getOpciones();
		int respuestaElegidaPreviamente = respuestasUsuario[indice];
		boolean respondida = respuestaElegidaPreviamente != -1;
		boolean modoConCorreccion = (modoActual == ModoTest.ESTUDIO && respondida) || modoActual == ModoTest.REPASO;

		if (modoConCorreccion) {
			boolean acierto = respondida && respuestaElegidaPreviamente == pregunta.getIndiceRespuestaCorrecta();
			Estilo.mostrarExplicacion(etiquetaExplicacion, pregunta.getExplicacion(), acierto);
		} else {
			Estilo.ocultarExplicacion(etiquetaExplicacion);
		}

		for (int i = 0; i < opciones.size(); i++) {
			int indiceOpcion = i;
			JButton botonOpcion = new JButton(opciones.get(i));

			if (modoConCorreccion) {
				botonOpcion.setEnabled(false);
				if (indiceOpcion == pregunta.getIndiceRespuestaCorrecta()) {
					Estilo.colorearSegunAcierto(botonOpcion, true);
				} else if (respondida && indiceOpcion == respuestaElegidaPreviamente) {
					Estilo.colorearSegunAcierto(botonOpcion, false);
				} else {
					Estilo.colorearNormal(botonOpcion);
				}
			} else if (modoActual == ModoTest.EXAMEN) {
				if (indiceOpcion == respuestaElegidaPreviamente) {
					Estilo.colorearComoMarcadaExamen(botonOpcion);
				} else {
					Estilo.colorearNormal(botonOpcion);
				}
				botonOpcion.addActionListener(e -> {
					respuestasUsuario[indicePreguntaActual] = indiceOpcion;
					generarBotoneraNumerica();
					mostrarPreguntaEspecifica(indicePreguntaActual);
				});
			} else {
				botonOpcion.addActionListener(e -> {
					respuestasUsuario[indicePreguntaActual] = indiceOpcion;
					generarBotoneraNumerica();
					mostrarPreguntaEspecifica(indicePreguntaActual);
				});
			}
			Estilo.configurarBotonOpcion(botonOpcion, panelOpcionesRespuesta, Constantes.ANCHO_BOTON_OPCION,
					modoActual == ModoTest.EXAMEN);
		}

		refrescarPanel(panelPreguntas);
	}

	/**
	 * Se ejecuta al finalizar el test (ya sea manualmente o por límite de tiempo).
	 * Delega en {@link LogicaTest#calcularResultado} el conteo de fallos y en
	 * {@link LogicaTest#apto} el veredicto (antes esa comparación con "<= 3" estaba
	 * escrita a mano aquí mismo), y pinta el resultado en pantalla.
	 */
	private static void calcularNotaYMostrarResultados() {
		int resultado = LogicaTest.calcularResultado(preguntasTestActual, respuestasUsuario);
		boolean aprobado = LogicaTest.apto(resultado);

		if (modoTestErrores) {
			guardarCorreccionesErrores();
		} else if (usuarioActual != null && testEnCurso != null) {
			ProgresoTest anterior = usuarioActual.getProgreso().get(testEnCurso.getId());
			Set<String> previas = anterior != null ? anterior.getIdsPreguntasFalladas() : new HashSet<>();
			Set<String> actualizadas = LogicaTest.actualizarPreguntasFalladas(previas, preguntasTestActual,
					respuestasUsuario);

			Map<String, Integer> respuestasPorId = new HashMap<>();
			for (int i = 0; i < preguntasTestActual.size(); i++) {
				respuestasPorId.put(preguntasTestActual.get(i).getId(), respuestasUsuario[i]);
			}

			usuarioActual.actualizarProgreso(testEnCurso.getId(), resultado, actualizadas, respuestasPorId);
			RepositorioBD.guardarProgresoTest(dniAlumnoLogueado, testEnCurso.getId(), resultado, actualizadas,
					respuestasPorId);
		}

		actualizarBotonOpcionErrores();

		if (modoTestErrores) {
			etiquetaNotaFinal.setText("Has fallado " + resultado + " preguntas.");
			etiquetaVeredictoFinal.setText(resultado != preguntasTestActual.size() ? "¡Has mejorado!" : "Casi...");
			etiquetaVeredictoFinal.setForeground(Constantes.COLOR_VEREDICTO_NEUTRO);
		} else {
			etiquetaNotaFinal.setText("Has fallado " + resultado + " preguntas.");
			etiquetaVeredictoFinal.setText(aprobado ? "APTO" : "NO APTO");
			etiquetaVeredictoFinal.setForeground(aprobado ? Constantes.COLOR_APTO : Constantes.COLOR_NO_APTO);
		}
		refrescarPanel(panelResultados);
		cambiarPanel(panelPreguntas, panelResultados);
	}

	/**
	 * Recorre las preguntas del repaso de errores y, por cada una acertada esta
	 * vez, la elimina del conjunto de falladas de su Test de origen (vía
	 * {@code origenPreguntaErrores}), persistiendo el cambio en MongoDB. No toca el
	 * contador histórico de fallos, solo el conjunto de IDs pendientes de repaso.
	 */
	private static void guardarCorreccionesErrores() {
		if (usuarioActual == null) {
			return;
		}
		for (int i = 0; i < preguntasTestActual.size(); i++) {
			Pregunta p = preguntasTestActual.get(i);
			boolean acierto = respuestasUsuario[i] == p.getIndiceRespuestaCorrecta();
			if (!acierto) {
				continue;
			}
			String idTestOrigen = origenPreguntaErrores.get(p.getId());
			ProgresoTest pt = idTestOrigen != null ? usuarioActual.getProgreso().get(idTestOrigen) : null;
			if (pt == null) {
				continue;
			}
			Set<String> falladasActualizadas = new HashSet<>(pt.getIdsPreguntasFalladas());
			falladasActualizadas.remove(p.getId());
			usuarioActual.actualizarProgreso(idTestOrigen, pt.getFallos(), falladasActualizadas);
			RepositorioBD.guardarProgresoTest(dniAlumnoLogueado, idTestOrigen, pt.getFallos(), falladasActualizadas);
		}
	}

	// ==========================================
	// SOPORTE DE VENTANAS
	// ==========================================

	/**
	 * Refresca visualmente un panel de Swing tras realizar modificaciones en sus
	 * componentes para garantizar que la pantalla se actualice correctamente.
	 * 
	 * @param panel El JPanel que requiere ser redibujado.
	 */
	public static void refrescarPanel(JPanel panel) {
		panel.revalidate();
		panel.repaint();
	}

	/**
	 * Realiza la transición visual entre dos pantallas del programa, ocultando la
	 * pantalla actual y adjuntando la nueva a la ventana principal.
	 * 
	 * @param panelViejo El JPanel que se va a retirar de la ventana.
	 * @param panelNuevo El JPanel que se va a mostrar en pantalla.
	 */
	private static void cambiarPanel(JPanel panelViejo, JPanel panelNuevo) {
		ventana.remove(panelViejo);
		ventana.add(panelNuevo);
		ventana.revalidate();
		ventana.repaint();
	}

	/**
	 * Inicializa las propiedades de la ventana principal de la aplicación,
	 * definiendo su comportamiento al cierre y ajustando sus proporciones de
	 * maximización.
	 */
	private static void configurarVentana() {
		ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ventana.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				ConexionBD.cerrarConexion();
			}
		});

		try {
			URL urlLogo = MainGUI.class.getResource(Constantes.LOGO);

			if (urlLogo != null) {
				Image imgNativa = new ImageIcon(urlLogo).getImage();

				List<Image> listaIconos = new ArrayList<>();

				listaIconos.add(imgNativa.getScaledInstance(Constantes.ICONO_PEQUENO, Constantes.ICONO_PEQUENO,
						Image.SCALE_SMOOTH));
				listaIconos.add(imgNativa.getScaledInstance(Constantes.ICONO_MEDIANO, Constantes.ICONO_MEDIANO,
						Image.SCALE_SMOOTH));
				listaIconos.add(imgNativa.getScaledInstance(Constantes.ICONO_GRANDE, Constantes.ICONO_GRANDE,
						Image.SCALE_SMOOTH));
				listaIconos.add(imgNativa.getScaledInstance(Constantes.ICONO_EXTRAGRANDE, Constantes.ICONO_EXTRAGRANDE,
						Image.SCALE_SMOOTH));

				ventana.setIconImages(listaIconos);
			} else {
				System.err.println("No se pudo encontrar el archivo del logo en los recursos.");
			}
		} catch (Exception e) {
			System.err.println("Error al cargar y procesar los iconos de la aplicación: " + e.getMessage());
		}

		tamañoVentanaSinMaximizar();
		ventana.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

	/**
	 * Establece unas proporciones base agradables para la ventana en caso de que el
	 * usuario decida restaurarla desde el estado maximizado.
	 */
	private static void tamañoVentanaSinMaximizar() {
		Rectangle limitePantalla = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		int anchoProporcional = (int) (limitePantalla.width * Constantes.ESCALA_PANTALLA_REDUCIDA);
		int altoProporcional = (int) (limitePantalla.height * Constantes.ESCALA_PANTALLA_REDUCIDA);
		ventana.setSize(anchoProporcional, altoProporcional);
		ventana.setLocationRelativeTo(null);
	}

	/**
	 * Sincroniza el texto del botón inferior del panel de Preguntas con el modo
	 * actual: "Volver a test" en Repaso (donde ya no se puede seguir respondiendo,
	 * solo consultar la corrección), "Finalizar test" en el resto de modos. No hace
	 * falta reestilizar nada más: {@link Estilo#configurarBotonEnlace} fija las
	 * dimensiones del botón de forma independiente al texto.
	 */
	private static void actualizarTextoBotonFinalizar() {
		botonFinalizarTest.setText(modoActual == ModoTest.REPASO ? "Volver a tests" : "Finalizar test");
	}

	/**
	 * Vincula la tecla Escape globalmente para que actúe como el botón de
	 * "volver"/"salir" del panel actualmente visible, sin tener que pulsarlo con el
	 * ratón. Usa un {@link KeyEventDispatcher} en vez de un listener por panel
	 * porque el foco puede estar en cualquier componente hijo (un campo de texto,
	 * un botón...) y el evento de teclado debe capturarse antes de que Swing lo
	 * consuma en el componente enfocado.
	 */
	private static void configurarAtajoEscape() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
			if (e.getID() != KeyEvent.KEY_PRESSED || e.getKeyCode() != KeyEvent.VK_ESCAPE) {
				return false;
			}
			if (panelAdmin.getParent() != null) {
				botonVolverInicioAdmin.doClick();
			} else if (panelTest.getParent() != null) {
				botonVolverTemas.doClick();
			} else if (panelTema.getParent() != null) {
				botonVolverInicioTema.doClick();
			} else if (panelResultados.getParent() != null) {
				botonSalirResultados.doClick();
			} else if (panelMenuPrincipal.getParent() != null) {
				botonVolverMenuPrincipal.doClick();
			}
			return false;
		});
	}
}
package app.constantes;

import java.awt.Color;
import java.awt.Font;

/**
 * Constantes de configuración del sistema, centralizadas en un único lugar para
 * evitar valores repetidos ("números mágicos") por el resto del código.
 * 
 * @author Ángel Hernández
 */
public final class Constantes {

	/** Clase de solo constantes: no debe instanciarse. */
	private Constantes() {
	}

	/** Longitud exacta que debe tener un DNI para considerarse válido. */
	public static final int LONGITUD_DNI = 9;

	/** Número máximo de fallos permitidos en un test para aprobarlo. */
	public static final int MAX_FALLOS_PARA_APROBAR = 3;

	/** Duración de un test, en segundos (30 minutos). */
	public static final int DURACION_TEST_SEGUNDOS = 30 * 60;

	/** Días que se suman a la licencia de un usuario al crearla o renovarla. */
	public static final int DIAS_VALIDEZ_LICENCIA = 30;

	/** Proporción de la pantalla que ocupa la ventana cuando no está maximizada. */
	public static final double ESCALA_PANTALLA_REDUCIDA = 0.75;

	/**
	 * Resolución de referencia sobre la que está diseñada la interfaz (la tuya).
	 */
	public static final int ANCHO_REFERENCIA_PANTALLA = 1920;
	public static final int ALTO_REFERENCIA_PANTALLA = 1080;

	/**
	 * Límites del factor de escalado para no deformar la UI en pantallas muy
	 * pequeñas o muy grandes.
	 */
	public static final double FACTOR_ESCALA_MINIMO = 0.85;
	public static final double FACTOR_ESCALA_MAXIMO = 1.0;

	/** Nombre de la base de datos en MongoDB Atlas. */
	public static final String NOMBRE_BD = "Ahevia";

	/** Nombre del archivo local de configuración (ver ConexionBD). */
	public static final String ARCHIVO_CONFIG = "config.properties";

	/** Clave dentro de config.properties donde se busca la URI de Mongo. */
	public static final String CLAVE_URI_CONFIG = "mongodb.uri";

	/** Nombre de la variable de entorno donde se busca la URI de Mongo. */
	public static final String VARIABLE_ENTORNO_URI = "MONGODB_URI";

	/** Enlace de contacto/soporte que abre el botón "Contáctenos" del Login. */
	public static final String URL_CONTACTO = "https://autoescuelaahe.github.io/Ahevia/";

	/** Longitud maxima que debe tener el campo para evitar ataques. */
	public static final int LONGITUD_MAXIMA = 100;

	// ===== Dimensiones compartidas por todas las tarjetas del sistema =====
	public static final int ANCHO_UNIVERSAL = 340;
	public static final int PADDING_VERTICAL_TARJETA = 60;
	public static final int PADDING_HORIZONTAL_TARJETA = 140;
	public static final int OPACIDAD_TARJETA = 204;

	// ===== Paleta de colores de los mensajes de aviso (error / éxito) =====
	public static final Color COLOR_TEXTO_ERROR = new Color(210, 30, 30);
	public static final Color COLOR_FONDO_ERROR = new Color(255, 235, 235);
	public static final Color COLOR_BORDE_ERROR = new Color(230, 50, 50);

	public static final Color COLOR_TEXTO_EXITO = new Color(30, 150, 60);
	public static final Color COLOR_FONDO_EXITO = new Color(235, 255, 240);
	public static final Color COLOR_BORDE_EXITO = new Color(50, 180, 80);

	// ===== Paleta de colores del motor de examen (corrección de respuestas) =====
	public static final Color COLOR_RESPUESTA_CORRECTA = new Color(40, 167, 69, 100);
	public static final Color COLOR_RESPUESTA_INCORRECTA = new Color(220, 53, 69, 100);

	// ===== Dimensiones específicas del panel de selección de Temas =====
	public static final int PADDING_VERTICAL_TARJETA_TEMAS = 40;
	public static final int PADDING_HORIZONTAL_TARJETA_TEMAS = 65;
	public static final int ANCHO_RECUADRO_TEMAS = 1400;
	public static final int ANCHO_BOTON_TEMA = 900;
	public static final int ALTO_RECUADRO_TEMAS = 731;
	public static final int ESPACIADO_BOTON_TEMA = 12;

	public static final int ANCHO_TEXTO_TEMA = 800;

	// ===== Colores y fuentes reutilizados en pintados/eventos frecuentes =====
	public static final Color COLOR_DEGRADADO_INICIO = new Color(41, 128, 185, 50);
	public static final Color COLOR_DEGRADADO_FIN = new Color(109, 213, 250, 50);

	public static final Font FUENTE_BOTON_PRINCIPAL = new Font("SansSerif", Font.BOLD, 16);
	public static final Color COLOR_BOTON_NORMAL = new Color(212, 228, 241);
	public static final Color COLOR_BOTON_PRESIONADO = new Color(192, 210, 225);

	public static final Color COLOR_ENLACE_NORMAL = new Color(100, 110, 120);
	public static final Color COLOR_ENLACE_HOVER = new Color(30, 35, 40);

	// --- COLORES GLOBALES DE LA INTERFAZ ---
	public static final Color COLOR_FONDO = new Color(202, 219, 230);
	public static final Color COLOR_TARJETA = Color.WHITE;
	public static final Color COLOR_BOTON_TEMA = new Color(212, 228, 240);
	public static final Color COLOR_HOVER_BOTON_TEMA = new Color(185, 208, 225);
	public static final Color COLOR_BORDE_BOTON_TEMA = new Color(44, 62, 80);
	public static final Color COLOR_TEXTO_BOTON = new Color(44, 62, 80);

	public static final int ANCHO_TEXTO_TESTS = 1200;

	// ===== Cuadrícula de selección numérica de Tests dentro de un Tema =====
	public static final int COLUMNAS_GRID_TESTS = 10;
	public static final int HGAP_GRID_TESTS = 30;
	public static final int VGAP_GRID_TESTS = 60;

	// ===== Panel de Preguntas (motor del examen) =====
	public static final int TAMANO_FUENTE_ENUNCIADO = 22;
	public static final int ALTO_ENUNCIADO = 65;
	public static final int ANCHO_BOTON_OPCION = 1000;
	public static final int ESPACIADO_BOTON_OPCION = 12;
	public static final int ANCHO_TEXTO_ENUNCIADO = 1000;

	public static final int ALTO_EXPLICACION = 160;
	public static final int ANCHO_TEXTO_EXPLICACION = 1200;

	public static final int ANCHO_EXPLICACIÓN_BOTONES = 1345;

	public static final int HGAP_BOTONES_NUMEROS = 6;
	public static final int ALTO_BOTON_NUMERO = 40;
	public static final int ESPACIADO_ARRIBA_BOTONES_NUMEROS = 20;
	public static final int HUECOS_BOTONERA = 30;

	// ===== Espaciado interno fijo del panel de Preguntas =====
	public static final int ESPACIADO_SUPERIOR_ENUNCIADO = 20;
	public static final int ESPACIADO_ENUNCIADO_OPCIONES = 15;
	public static final int ESPACIADO_OPCIONES_EXPLICACION = 35;
	public static final int ESPACIADO_EXPLICACION_BOTONES = 20;

	// ===== Cálculo del margen superior de la caja de explicación =====
	public static final int MARGEN_SUPERIOR_MINIMO_EXPLICACION = 12;
	public static final int MARGEN_SUPERIOR_MAXIMO_EXPLICACION = 95;
	public static final int MARGEN_LATERAL_EXPLICACION = 20;
	public static final int MARGEN_INFERIOR_EXPLICACION = 10;
	public static final int TAMANO_FUENTE_HTML_EXPLICACION = 4;

	// --- NUEVOS COLORES SUAVES Y ELEGANTES ---
	public static final Color COLOR_RESPUESTA_CORRECTA_PRESIONADO = new Color(169, 223, 183);
	public static final Color COLOR_RESPUESTA_INCORRECTA_PRESIONADO = new Color(0xF5B7B1);

	public static final Color COLOR_RESPUESTA_EXAMEN_MARCADA = COLOR_BOTON_PRESIONADO;
	public static final Color COLOR_RESPUESTA_EXAMEN_MARCADA_PRESIONADO = new Color(170, 190, 208);

	public static final Color COLOR_APTO = Color.GREEN;

	public static final Color COLOR_NO_APTO = Color.RED;

	public static final Color COLOR_AZUL_BLANCO = new Color(228, 240, 250);

	// ===== Flechas de navegación entre preguntas (Anterior / Siguiente) =====
	public static final int ESPACIADO_FLECHA_OPCIONES = 40;
	public static final int ANCHO_BOTON_FLECHA = 50;
	public static final int ALTO_BOTON_FLECHA = 50;
	public static final int ESPACIADO_FLECHA_BORDE = 20;
	public static final Color COLOR_FLECHA_DESHABILITADA = new Color(190, 190, 190);

	public static final int SEMIANCHO_TRIANGULO_FLECHA = 7;
	public static final int SEMIALTO_TRIANGULO_FLECHA = 12;

	public static final Color COLOR_TEST_BIEN = new Color(198, 239, 206);
	public static final Color COLOR_TEST_BIEN_PRESIONADO = new Color(169, 223, 183);
	public static final Color COLOR_TEST_REGULAR = new Color(255, 236, 179);
	public static final Color COLOR_TEST_REGULAR_PRESIONADO = new Color(255, 213, 128);
	public static final Color COLOR_TEST_MAL = new Color(248, 215, 218);
	public static final Color COLOR_TEST_MAL_PRESIONADO = new Color(245, 183, 177);

	public static final String TITULO_TEMA_ERRORES = "Test de errores";
	public static final String ID_OPCION_ALEATORIOS = "OPCION1";
	public static final String ID_OPCION_ERRORES = "OPCION2";
	public static final String TITULO_MENU_PRINCIPAL = "¿Qué desea hacer?";
	public static final String TEXTO_OPCION_TEMAS = "Tests por temas";

	public static final Color COLOR_BOTON_DESHABILITADO = new Color(225, 225, 225);

	public static final Color COLOR_VEREDICTO_NEUTRO = new Color(41, 128, 185);

	public static final String TEXTO_CRONOMETRO_ERRORES = "Repaso de errores";

	public static final String TEXTO_CRONOMETRO_ESTUDIO = "Test modo estudio";

	public static final String TEXTO_CRONOMETRO_REPASO = "Repaso del test";

	public static final String LOGO = "Logo circular.png";

	// ===== Imagen asociada a la pregunta (ahora integrada en el enunciado) =====
	public static final String URL_BASE_IMAGENES = "https://raw.githubusercontent.com/AngelHernandeezz/public-app-tests-dgt/main/Imagenes/";
	public static final int ANCHO_DIALOGO_IMAGEN = 600;

	// ===== Estilo del enunciado cuando tiene imagen asociada (clicable) =====
	public static final Color COLOR_TEXTO_ENUNCIADO_NORMAL = Color.BLACK;
	public static final Color COLOR_TEXTO_ENUNCIADO_IMAGEN = new Color(40, 70, 130);
	public static final Color COLOR_TEXTO_ENUNCIADO_IMAGEN_HOVER = new Color(25, 50, 100);
	public static final String TOOLTIP_ENUNCIADO_IMAGEN = "Haz clic para ver la imagen asociada";

	// ===== Formato de fecha =====
	public static final String FORMATO_FECHA = "dd/MM/yyyy";

	// ===== Iconos de ventana =====
	public static final int ICONO_PEQUENO = 16;
	public static final int ICONO_MEDIANO = 32;
	public static final int ICONO_GRANDE = 48;
	public static final int ICONO_EXTRAGRANDE = 64;

	// ===== Espaciados verticales estándar entre bloques de tarjeta =====
	public static final int ESPACIADO_PEQUENO = 15;
	public static final int ESPACIADO_MEDIO = 20;
	public static final int ESPACIADO_ESTANDAR = 25;
	public static final int ESPACIADO_GRANDE = 30;
	public static final int DESPLAZAMIENTO_TARJETA_CLASICA = 90;

	// ===== Dimensiones estándar de componentes =====
	public static final int ALTO_BOTON_ESTANDAR = 45;
	public static final int ANCHO_BOTON_CUADRADO = 80;
	public static final int ALTO_BOTON_CUADRADO = 50;
	public static final int ALTO_BOTON_ENLACE = 25;
	public static final int GROSOR_BORDE_SELECCION = 3;

	// ===== Tipografía por componente =====
	public static final int TAMANO_FUENTE_TITULO = 21;
	public static final int TAMANO_FUENTE_VEREDICTO = 36;
	public static final int TAMANO_FUENTE_ENLACE = 14;
	public static final int TAMANO_FUENTE_MENSAJE = 13;
	public static final int TAMANO_FUENTE_BOTON_CUADRADO = 16;

	// ===== Título dinámico (ajuste de ancho al texto) =====
	public static final int ALTO_TITULO = 30;
	public static final int MARGEN_EXTRA_TITULO = 25;

	// ===== Veredicto final =====
	public static final int ALTO_VEREDICTO = 50;

	// ===== Caja de mensaje dinámico (error/éxito) =====
	public static final int ALTO_MENSAJE = 50;
	public static final int DESPLAZAMIENTO_VERTICAL_MENSAJE = 15;

	// ===== Margen interno caja de explicación =====
	public static final int MARGEN_SUPERIOR_CAJA_EXPLICACION = 5;
	public static final int MARGEN_LATERAL_CAJA_EXPLICACION = 10;

	// ===== Padding de grids con scroll =====
	public static final int PADDING_SUPERIOR_GRID_TESTS = 50;
	public static final int PADDING_LATERAL_GRID_TESTS = 20;
	public static final int PADDING_SUPERIOR_LISTA_TEMAS = 20;
	public static final int PADDING_LATERAL_LISTA_TEMAS = 10;
	public static final int PADDING_VERTICAL_CONTENEDOR_PREGUNTAS = 30;

}
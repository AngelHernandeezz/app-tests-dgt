package app.estilo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import app.MainGUI;
import app.constantes.Constantes;
import app.logica.EstadoTest;

/**
 * Clase con todo el estilo aplicado a Java Swing.
 * 
 * @author Ángel Hernández
 */
public class Estilo {

	/** Clase de solo métodos estáticos: no debe instanciarse. */
	private Estilo() {
	}

	// ===== Escalado de la interfaz según resolución real de pantalla =====
	private static double factorEscala = 1.0;

	/**
	 * Calcula el factor de escalado de la interfaz según la resolución real de
	 * pantalla frente a la resolución de referencia
	 * ({@link Constantes#ANCHO_REFERENCIA_PANTALLA}). Debe llamarse una sola vez en
	 * main(), antes de aplicar cualquier diseño de panel. Sin esto, en pantallas
	 * más pequeñas que la de referencia el recuadro con scroll (Temas/Tests/
	 * Preguntas) queda más grande que la ventana disponible y tapa contenido con su
	 * propia barra de scroll.
	 *
	 * @param anchoPantallaReal Ancho útil de la pantalla del usuario, en píxeles.
	 * @param altoPantallaReal  Alto útil de la pantalla del usuario, en píxeles.
	 */
	public static void inicializarEscala(int anchoPantallaReal, int altoPantallaReal) {
		double factorX = (double) anchoPantallaReal / Constantes.ANCHO_REFERENCIA_PANTALLA;
		double factorY = (double) altoPantallaReal / Constantes.ALTO_REFERENCIA_PANTALLA;
		factorEscala = Math.min(factorX, factorY);
		factorEscala = Math.max(Constantes.FACTOR_ESCALA_MINIMO,
				Math.min(Constantes.FACTOR_ESCALA_MAXIMO, factorEscala));
	}

	/**
	 * Escala un valor en píxeles según el factor calculado en
	 * {@link #inicializarEscala}.
	 */
	private static int escalar(int valor) {
		return (int) Math.round(valor * factorEscala);
	}

	/**
	 * METODO REUTILIZABLE: Escala una {@link Dimension} completa (ancho y alto) de
	 * una sola vez, usada en todos los métodos "configurarX" para que los
	 * componentes internos (botones, campos, etiquetas...) se adapten igual que ya
	 * lo hacen los contenedores/recuadros que los envuelven.
	 */
	private static Dimension escalarDimension(int ancho, int alto) {
		return new Dimension(escalar(ancho), escalar(alto));
	}

	/**
	 * METODO REUTILIZABLE: Escala el tamaño de una fuente igual que cualquier otra
	 * medida en píxeles, con un tamaño mínimo de 8 para que el texto no deje de
	 * poder leerse en pantallas muy pequeñas.
	 */
	private static int escalarFuente(int tamano) {
		return Math.max(8, escalar(tamano));
	}

	/**
	 * METODO REUTILIZABLE: Colorea el fondo de un botón de Test del grid según el
	 * estado de progreso calculado por
	 * {@link app.logica.LogicaTest#clasificarTest}. Si el estado es
	 * {@code NO_HECHO} no se toca el color, dejando el azul por defecto de
	 * {@link #configurarBotonCuadradoTest}.
	 *
	 * @param boton  El botón de Test a colorear.
	 * @param estado El estado de progreso ya clasificado.
	 */
	public static void colorearSegunProgreso(JButton boton, EstadoTest estado) {
		switch (estado) {
		case BIEN:
			boton.setBackground(Constantes.COLOR_TEST_BIEN);
			break;
		case REGULAR:
			boton.setBackground(Constantes.COLOR_TEST_REGULAR);
			break;
		case MAL:
			boton.setBackground(Constantes.COLOR_TEST_MAL);
			break;
		default:
			break;
		}
	}

	/**
	 * METODO REUTILIZABLE: Monta la fila horizontal del panel de Preguntas que
	 * combina las flechas de navegación Anterior/Siguiente con el panel central de
	 * opciones de respuesta, aplicando a las flechas su estilo
	 * ({@link #configurarBotonFlecha}) antes de ensamblarlas.
	 *
	 * @param botonAnterior          Flecha que retrocede una pregunta.
	 * @param botonSiguiente         Flecha que avanza una pregunta.
	 * @param panelOpcionesRespuesta El panel central con los botones de opción.
	 * @return La fila ya ensamblada, lista para añadirse al contenedor de
	 *         preguntas.
	 */
	public static JPanel crearFilaOpcionesConFlechas(JButton botonAnterior, JButton botonSiguiente,
			JPanel panelOpcionesRespuesta) {
		JPanel filaOpciones = new JPanel();
		filaOpciones.setLayout(new BoxLayout(filaOpciones, BoxLayout.X_AXIS));
		filaOpciones.setOpaque(false);
		filaOpciones.setAlignmentX(Component.CENTER_ALIGNMENT);

		configurarBotonFlecha(botonAnterior, false);
		configurarBotonFlecha(botonSiguiente, true);

		filaOpciones.add(Box.createHorizontalStrut(escalar(Constantes.ESPACIADO_FLECHA_BORDE)));
		filaOpciones.add(botonAnterior);
		filaOpciones.add(Box.createHorizontalStrut(escalar(Constantes.ESPACIADO_FLECHA_OPCIONES)));
		filaOpciones.add(panelOpcionesRespuesta);
		filaOpciones.add(Box.createHorizontalStrut(escalar(Constantes.ESPACIADO_FLECHA_OPCIONES)));
		filaOpciones.add(botonSiguiente);
		filaOpciones.add(Box.createHorizontalStrut(escalar(Constantes.ESPACIADO_FLECHA_BORDE)));

		return filaOpciones;
	}

	/**
	 * METODO REUTILIZABLE: Da estilo a una flecha de navegación entre preguntas
	 * (Anterior/Siguiente), dibujándola a mano como un triángulo simétrico vía
	 * Graphics2D en vez de usar texto o un icono, para que escale sin pixelarse y
	 * cambie de color limpiamente según su estado (normal/hover/deshabilitada).
	 *
	 * @param botonFlecha   El botón a estilizar, sin texto ni fondo nativo.
	 * @param apuntaDerecha {@code true} para que el triángulo apunte a la derecha
	 *                      (Siguiente), {@code false} para que apunte a la
	 *                      izquierda (Anterior).
	 */
	public static void configurarBotonFlecha(JButton botonFlecha, boolean apuntaDerecha) {
		botonFlecha.setText("");
		botonFlecha.setFocusPainted(false);
		botonFlecha.setContentAreaFilled(false);
		botonFlecha.setBorderPainted(false);
		botonFlecha.setOpaque(false);
		botonFlecha.setRolloverEnabled(true);
		botonFlecha.setCursor(new Cursor(Cursor.HAND_CURSOR));
		botonFlecha.setAlignmentY(Component.CENTER_ALIGNMENT);

		Dimension dimensionFlecha = escalarDimension(Constantes.ANCHO_BOTON_FLECHA, Constantes.ALTO_BOTON_FLECHA);
		botonFlecha.setMinimumSize(dimensionFlecha);
		botonFlecha.setPreferredSize(dimensionFlecha);
		botonFlecha.setMaximumSize(dimensionFlecha);

		botonFlecha.setUI(new BasicButtonUI() {
			@Override
			public void paint(Graphics g, JComponent c) {
				AbstractButton b = (AbstractButton) c;
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				Color colorTrazo;
				if (!b.isEnabled()) {
					colorTrazo = Constantes.COLOR_FLECHA_DESHABILITADA;
				} else if (b.getModel().isRollover() || b.getModel().isPressed()) {
					colorTrazo = Constantes.COLOR_ENLACE_HOVER;
				} else {
					colorTrazo = new Color(40, 45, 50);
				}

				int cx = c.getWidth() / 2;
				int cy = c.getHeight() / 2;
				int semiAncho = escalar(Constantes.SEMIANCHO_TRIANGULO_FLECHA);
				int semiAlto = escalar(Constantes.SEMIALTO_TRIANGULO_FLECHA);

				Path2D.Double triangulo = new Path2D.Double();
				if (apuntaDerecha) {
					triangulo.moveTo(cx - semiAncho, cy - semiAlto);
					triangulo.lineTo(cx + semiAncho, cy);
					triangulo.lineTo(cx - semiAncho, cy + semiAlto);
				} else {
					triangulo.moveTo(cx + semiAncho, cy - semiAlto);
					triangulo.lineTo(cx - semiAncho, cy);
					triangulo.lineTo(cx + semiAncho, cy + semiAlto);
				}
				triangulo.closePath();

				g2d.setColor(colorTrazo);
				g2d.fill(triangulo);
				g2d.dispose();
			}
		});
	}

	/**
	 * Aplica al panel de Preguntas la misma base visual que el panel de Tests:
	 * mismo fondo degradado, misma tarjeta y mismo recuadro de tamaño fijo. El
	 * cronómetro ocupa el lugar del título (arriba del todo) y el botón Finalizar
	 * Test ocupa el lugar del enlace inferior ("Volver a..."), con el mismo estilo
	 * de enlace que el resto de la app.
	 *
	 * <p>
	 * Además, monta aquí mismo la estructura interna fija (enunciado, opciones,
	 * explicación y botonera numérica) una única vez, igual que el resto de los
	 * métodos {@code aplicarDisenoPanelX}. Antes esa estructura se reconstruía en
	 * MainGUI cada vez que arrancaba un test nuevo con los mismos componentes y
	 * separadores de siempre, lo que era trabajo repetido e innecesario.
	 * 
	 * @param panelPreguntas         El panel raíz de la pantalla de Preguntas.
	 * @param etiquetaCronometro     La etiqueta donde se muestra el cronómetro (o
	 *                               el título del modo de test).
	 * @param paneEnunciado          El componente donde se pinta el enunciado.
	 * @param panelOpcionesRespuesta El panel donde se listan las opciones de
	 *                               respuesta.
	 * @param etiquetaExplicacion    El componente donde se pinta la explicación
	 *                               tras responder.
	 * @param panelBotonesNumeros    El panel de la botonera numérica de navegación
	 *                               entre preguntas.
	 * @param botonFinalizarTest     El botón inferior de fin de test.
	 * @param botonPreguntaAnterior  La flecha que retrocede una pregunta.
	 * @param botonPreguntaSiguiente La flecha que avanza una pregunta.
	 *
	 * @return El panel interno (el recuadro), ya con el enunciado, las opciones y
	 *         la botonera numérica añadidos. MainGUI ya no toca su estructura: solo
	 *         actualiza el contenido dinámico de cada uno en cada pregunta o test
	 *         nuevo.
	 */
	public static JPanel aplicarDisenoPanelPreguntas(JPanel panelPreguntas, JLabel etiquetaCronometro,
			JTextPane paneEnunciado, JPanel panelOpcionesRespuesta, JTextPane etiquetaExplicacion,
			JPanel panelBotonesNumeros, JButton botonFinalizarTest, JButton botonPreguntaAnterior,
			JButton botonPreguntaSiguiente) {

		int anchoUniversal = Constantes.ANCHO_UNIVERSAL;

		JPanel contenedorBlanco = prepararContenedor(panelPreguntas, Constantes.PADDING_VERTICAL_TARJETA_TEMAS,
				Constantes.PADDING_HORIZONTAL_TARJETA_TEMAS, Constantes.OPACIDAD_TARJETA, 0);

		configurarTitulo(etiquetaCronometro, Constantes.ANCHO_TEXTO_TESTS, etiquetaCronometro.getText());

		JPanel contenedorPreguntas = new JPanel();
		contenedorPreguntas.setLayout(new BoxLayout(contenedorPreguntas, BoxLayout.Y_AXIS));
		contenedorPreguntas.setOpaque(false);
		contenedorPreguntas.setBorder(
				BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 224, 230, 150), 1),
						new EmptyBorder(Constantes.PADDING_VERTICAL_CONTENEDOR_PREGUNTAS, 0,
								Constantes.PADDING_VERTICAL_CONTENEDOR_PREGUNTAS, 0)));

		Dimension dimensionRecuadro = new Dimension(escalar(Constantes.ANCHO_RECUADRO_TEMAS),
				escalar(Constantes.ALTO_RECUADRO_TEMAS));
		contenedorPreguntas.setMinimumSize(dimensionRecuadro);
		contenedorPreguntas.setPreferredSize(dimensionRecuadro);
		contenedorPreguntas.setMaximumSize(dimensionRecuadro);

		configurarEnunciado(paneEnunciado, Constantes.ANCHO_TEXTO_ENUNCIADO);
		configurarExplicacion(etiquetaExplicacion, Constantes.ANCHO_EXPLICACIÓN_BOTONES, Constantes.ALTO_EXPLICACION);
		configurarPanelBotonesNumeros(panelBotonesNumeros);

		panelOpcionesRespuesta.setLayout(new BoxLayout(panelOpcionesRespuesta, BoxLayout.Y_AXIS));
		panelOpcionesRespuesta.setOpaque(false);
		panelOpcionesRespuesta.setAlignmentX(Component.CENTER_ALIGNMENT);

		contenedorPreguntas.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_SUPERIOR_ENUNCIADO)));
		contenedorPreguntas.add(paneEnunciado);
		contenedorPreguntas.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_ENUNCIADO_OPCIONES)));
		contenedorPreguntas.add(Estilo.crearFilaOpcionesConFlechas(botonPreguntaAnterior, botonPreguntaSiguiente,
				panelOpcionesRespuesta));
		contenedorPreguntas.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_OPCIONES_EXPLICACION)));
		contenedorPreguntas.add(etiquetaExplicacion);
		contenedorPreguntas.add(Box.createVerticalGlue());
		contenedorPreguntas.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_EXPLICACION_BOTONES)));
		contenedorPreguntas.add(panelBotonesNumeros);

		configurarBotonEnlace(botonFinalizarTest, anchoUniversal, botonFinalizarTest.getText());
		contenedorBlanco.add(etiquetaCronometro);
		contenedorBlanco.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_MEDIO)));
		contenedorBlanco.add(contenedorPreguntas);
		contenedorBlanco.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_ESTANDAR)));
		contenedorBlanco.add(botonFinalizarTest);

		MainGUI.refrescarPanel(panelPreguntas);
		return contenedorPreguntas;
	}

	/**
	 * METODO REUTILIZABLE: Configura el contenedor de la botonera numérica para que
	 * sus 30 huecos repartan el mismo ancho que el recuadro de explicación
	 * ({@link Constantes#ANCHO_EXPLICACIÓN_BOTONES}), con espaciado entre los
	 * botones (que de paso los hace más compactos, ya que GridLayout resta ese
	 * hueco del ancho disponible por celda) y un margen superior que los separa de
	 * la explicación de arriba. GridLayout(1, 30) fuerza una sola fila de columnas
	 * fijas, independientemente del número real de preguntas del test.
	 */
	public static void configurarPanelBotonesNumeros(JPanel panelBotonesNumeros) {
		panelBotonesNumeros
				.setLayout(new GridLayout(0, Constantes.HUECOS_BOTONERA, escalar(Constantes.HGAP_BOTONES_NUMEROS), 0));
		panelBotonesNumeros.setOpaque(false);
		panelBotonesNumeros.setAlignmentX(Component.CENTER_ALIGNMENT);
		panelBotonesNumeros.setBorder(new EmptyBorder(escalar(Constantes.ESPACIADO_ARRIBA_BOTONES_NUMEROS), 0, 0, 0));

		int altoTotal = Constantes.ESPACIADO_ARRIBA_BOTONES_NUMEROS + Constantes.ALTO_BOTON_NUMERO;
		Dimension dimensionBotonera = escalarDimension(Constantes.ANCHO_EXPLICACIÓN_BOTONES, altoTotal);
		panelBotonesNumeros.setMinimumSize(dimensionBotonera);
		panelBotonesNumeros.setPreferredSize(dimensionBotonera);
		panelBotonesNumeros.setMaximumSize(dimensionBotonera);
	}

	public static void configurarExplicacion(JTextPane etiquetaExplicacion, int ancho, int alto) {
		etiquetaExplicacion.setContentType("text/html");
		etiquetaExplicacion.setEditable(false);
		etiquetaExplicacion.setFocusable(false);
		etiquetaExplicacion.setOpaque(false);
		etiquetaExplicacion.setFont(new Font("SansSerif", Font.PLAIN, escalarFuente(14)));
		etiquetaExplicacion.setAlignmentX(Component.CENTER_ALIGNMENT);

		SimpleAttributeSet centrado = new SimpleAttributeSet();
		StyleConstants.setAlignment(centrado, StyleConstants.ALIGN_CENTER);
		etiquetaExplicacion.setParagraphAttributes(centrado, true);

		Dimension dimensionExplicacion = escalarDimension(ancho, alto);
		etiquetaExplicacion.setMinimumSize(dimensionExplicacion);
		etiquetaExplicacion.setPreferredSize(dimensionExplicacion);
		etiquetaExplicacion.setMaximumSize(dimensionExplicacion);

		int mSup = escalar(Constantes.MARGEN_SUPERIOR_CAJA_EXPLICACION);
		int mLat = escalar(Constantes.MARGEN_LATERAL_CAJA_EXPLICACION);
		etiquetaExplicacion.setMargin(new Insets(mSup, mLat, mSup, mLat));
	}

	/**
	 * METODO REUTILIZABLE: Pinta la caja de explicación tras responder una
	 * pregunta. Calcula su margen superior según la longitud del texto para que
	 * quede centrada verticalmente dentro de la caja de alto fijo
	 * ({@link Constantes#ALTO_EXPLICACION}), y la colorea en verde o rojo según el
	 * acierto. Antes este cálculo (la "fórmula matemática para el centrado
	 * vertical") vivía dentro de MainGUI; se traslada aquí porque es puro estilo
	 * visual, sin ninguna regla de negocio.
	 */
	public static void mostrarExplicacion(JTextPane etiquetaExplicacion, String explicacion, boolean acierto) {
		Color colorBorde = acierto ? Constantes.COLOR_BORDE_EXITO : Constantes.COLOR_BORDE_ERROR;
		Color colorFondo = acierto ? Constantes.COLOR_FONDO_EXITO : Constantes.COLOR_FONDO_ERROR;

		int anchoDisponibleSinEscalar = Constantes.ANCHO_EXPLICACIÓN_BOTONES - 2 * Constantes.MARGEN_LATERAL_EXPLICACION
				- 2 * Constantes.MARGEN_LATERAL_CAJA_EXPLICACION;

		JTextPane medidor = new JTextPane();
		medidor.setContentType("text/html");
		medidor.setFont(new Font("SansSerif", Font.PLAIN, 11));
		medidor.setText("<html><center><span style='font-family:SansSerif; font-size:11px;'><b>" + explicacion
				+ "</b></span></center></html>");
		medidor.setSize(anchoDisponibleSinEscalar, Short.MAX_VALUE);
		int altoTextoEstimado = medidor.getPreferredSize().height;

		int margenSuperiorY = (Constantes.ALTO_EXPLICACION - altoTextoEstimado) / 2;
		if (margenSuperiorY < Constantes.MARGEN_SUPERIOR_MINIMO_EXPLICACION) {
			margenSuperiorY = Constantes.MARGEN_SUPERIOR_MINIMO_EXPLICACION;
		}
		if (margenSuperiorY > Constantes.MARGEN_SUPERIOR_MAXIMO_EXPLICACION) {
			margenSuperiorY = Constantes.MARGEN_SUPERIOR_MAXIMO_EXPLICACION;
		}

		etiquetaExplicacion.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(colorBorde, 1),
				BorderFactory.createEmptyBorder(escalar(margenSuperiorY),
						escalar(Constantes.MARGEN_LATERAL_EXPLICACION), escalar(Constantes.MARGEN_INFERIOR_EXPLICACION),
						escalar(Constantes.MARGEN_LATERAL_EXPLICACION))));

		etiquetaExplicacion.setBackground(colorFondo);
		etiquetaExplicacion.setOpaque(true);

		int tamanoFuentePx = escalarFuente(11);
		etiquetaExplicacion.setText("<html><center><span style='font-family:SansSerif; font-size:" + tamanoFuentePx
				+ "px;'><b>" + explicacion + "</b></span></center></html>");
	}

	/**
	 * METODO REUTILIZABLE: Oculta por completo la caja de explicación (sin borde,
	 * sin fondo y sin texto), para cuando la pregunta todavía no ha sido
	 * respondida.
	 */
	public static void ocultarExplicacion(JTextPane etiquetaExplicacion) {
		etiquetaExplicacion.setOpaque(false);
		etiquetaExplicacion.setBorder(null);
		etiquetaExplicacion.setText("");
	}

	/**
	 * METODO REUTILIZABLE: Da estilo al enunciado de la pregunta. Usa JTextPane en
	 * vez de JLabel porque JLabel no envuelve el texto de forma fiable cuando
	 * supera el ancho disponible; JTextPane sí hace salto de línea natural.
	 *
	 * <p>
	 * Además, deja preparado el efecto hover que intensifica el color azul del
	 * propio texto del enunciado cuando la pregunta actual tiene imagen asociada:
	 * el enunciado hace ahora las veces del antiguo botón de texto "Ver imagen",
	 * pero sin caja ni borde, solo cambiando el color del texto (ver
	 * {@link #actualizarEstiloEnunciado}). Si la pregunta no tiene imagen, el hover
	 * no hace nada.
	 */
	public static void configurarEnunciado(JTextPane paneEnunciado, int ancho) {
		paneEnunciado.setEditable(false);
		paneEnunciado.setFocusable(false);
		paneEnunciado.setOpaque(false);
		paneEnunciado.setBorder(null);
		paneEnunciado.setFont(new Font("SansSerif", Font.BOLD, escalarFuente(Constantes.TAMANO_FUENTE_ENUNCIADO)));
		paneEnunciado.setAlignmentX(Component.CENTER_ALIGNMENT);
		paneEnunciado.setForeground(Constantes.COLOR_TEXTO_ENUNCIADO_NORMAL);

		Dimension dimensionEnunciado = escalarDimension(ancho, Constantes.ALTO_ENUNCIADO);
		paneEnunciado.setMinimumSize(dimensionEnunciado);
		paneEnunciado.setPreferredSize(dimensionEnunciado);
		paneEnunciado.setMaximumSize(dimensionEnunciado);

		paneEnunciado.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (Boolean.TRUE.equals(paneEnunciado.getClientProperty("tieneImagen"))) {
					paneEnunciado.setForeground(Constantes.COLOR_TEXTO_ENUNCIADO_IMAGEN_HOVER);
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (Boolean.TRUE.equals(paneEnunciado.getClientProperty("tieneImagen"))) {
					paneEnunciado.setForeground(Constantes.COLOR_TEXTO_ENUNCIADO_IMAGEN);
				}
			}
		});
	}

	/**
	 * METODO REUTILIZABLE: Activa o desactiva el resaltado del enunciado de la
	 * pregunta según si tiene imagen asociada
	 * ({@link app.modelo.Pregunta#isTieneImagen()}). Sustituye al antiguo botón de
	 * texto "Ver imagen": ahora es el propio texto del enunciado el que cambia a
	 * azul, con cursor de mano y tooltip, actuando como enlace clicable hacia la
	 * imagen. No toca tamaño, borde ni fondo: solo el color del texto.
	 */
	public static void actualizarEstiloEnunciado(JTextPane paneEnunciado, boolean tieneImagen) {
		paneEnunciado.putClientProperty("tieneImagen", tieneImagen);
		paneEnunciado.setCursor(new Cursor(tieneImagen ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
		paneEnunciado.setToolTipText(tieneImagen ? Constantes.TOOLTIP_ENUNCIADO_IMAGEN : null);
		paneEnunciado.setForeground(
				tieneImagen ? Constantes.COLOR_TEXTO_ENUNCIADO_IMAGEN : Constantes.COLOR_TEXTO_ENUNCIADO_NORMAL);
	}

	/**
	 * METODO REUTILIZABLE: Cambia el texto de un JTextPane manteniendo el párrafo
	 * centrado. La alineación se pierde en cada setText() porque reescribe el
	 * documento interno, así que hay que reaplicarla cada vez que cambia el texto.
	 */
	public static void establecerTextoCentrado(JTextPane panelTexto, String texto) {
		panelTexto.setText(texto);
		StyledDocument documento = panelTexto.getStyledDocument();
		SimpleAttributeSet centrado = new SimpleAttributeSet();
		StyleConstants.setAlignment(centrado, StyleConstants.ALIGN_CENTER);
		documento.setParagraphAttributes(0, documento.getLength(), centrado, false);
	}

	/**
	 * METODO REUTILIZABLE: Da estilo a un botón de opción de respuesta reutilizando
	 * el mismo aspecto que los botones principales de la app, y lo añade ya al
	 * panel de opciones con el espaciado inferior necesario para quedar apilados
	 * uno debajo de otro (mismo patrón que {@link #configurarBotonTema}).
	 *
	 * <p>
	 * A diferencia de {@code configurarBotonPrincipal}, el pintado consulta
	 * {@code getBackground()} cuando el botón está deshabilitado: así, una vez
	 * contestada la pregunta, respeta el verde/rojo/azul que MainGUI asigna con
	 * {@code setBackground(...)} para marcar la corrección.
	 *
	 * <p>
	 * FIX: {@code paintText} se sobreescribe por completo (sin llamar a
	 * {@code super.paintText}) porque {@link BasicButtonUI} pinta el texto de un
	 * botón deshabilitado con un efecto en relieve (claro/oscuro sobre el fondo),
	 * ignorando el {@code setForeground(...)} que se fija más abajo. Al
	 * deshabilitar el botón con {@code setEnabled(false)} tras responder, ese
	 * efecto dejaba las letras "feas" en vez de negras. Ahora se pinta siempre con
	 * el foreground del botón, esté habilitado o no.
	 *
	 * @param botonOpcion            El botón de opción a estilizar.
	 * @param panelOpcionesRespuesta El panel al que se añade el botón ya
	 *                               estilizado.
	 * @param ancho                  El ancho del botón, sin escalar.
	 * @param mostrarPresionado      Si {@code true}, el botón pinta
	 *                               {@link Constantes#COLOR_BOTON_PRESIONADO}
	 *                               mientras se mantiene pulsado (modo Examen,
	 *                               donde ese mismo color se conserva al soltar
	 *                               como marca de "respondida"). Si {@code false},
	 *                               el botón nunca pinta el color de pulsado (modo
	 *                               Estudio, donde el clic dispara corrección
	 *                               inmediata y ese destello intermedio no aporta
	 *                               nada antes del verde/rojo).
	 */
	public static void configurarBotonOpcion(JButton botonOpcion, JPanel panelOpcionesRespuesta, int ancho,
			boolean mostrarPresionado) {
		botonOpcion.setAlignmentX(Component.CENTER_ALIGNMENT);
		botonOpcion.setFont(
				new Font(Constantes.FUENTE_BOTON_PRINCIPAL.getName(), Constantes.FUENTE_BOTON_PRINCIPAL.getStyle(),
						escalarFuente(Constantes.FUENTE_BOTON_PRINCIPAL.getSize())));

		Dimension dimensionBoton = escalarDimension(ancho, Constantes.ALTO_BOTON_ESTANDAR);
		botonOpcion.setMinimumSize(dimensionBoton);
		botonOpcion.setPreferredSize(dimensionBoton);
		botonOpcion.setMaximumSize(dimensionBoton);
		botonOpcion.setForeground(new Color(40, 45, 50));
		botonOpcion.setFocusPainted(false);
		botonOpcion.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		botonOpcion.setContentAreaFilled(false);
		botonOpcion.setOpaque(false);
		botonOpcion.setCursor(new Cursor(Cursor.HAND_CURSOR));

		botonOpcion.setUI(new BasicButtonUI() {
			@Override
			public void paint(Graphics g, JComponent c) {
				AbstractButton b = (AbstractButton) c;
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				Color fondoMarcado = b.getBackground();
				boolean tieneMarcaExamen = fondoMarcado != null
						&& fondoMarcado.equals(Constantes.COLOR_RESPUESTA_EXAMEN_MARCADA);

				if (!b.isEnabled()) {
					g2d.setColor(fondoMarcado);
				} else if (tieneMarcaExamen) {
					g2d.setColor(b.getModel().isPressed() ? Constantes.COLOR_RESPUESTA_EXAMEN_MARCADA_PRESIONADO
							: Constantes.COLOR_RESPUESTA_EXAMEN_MARCADA);
				} else if (mostrarPresionado && b.getModel().isPressed()) {
					g2d.setColor(Constantes.COLOR_BOTON_PRESIONADO);
				} else {
					g2d.setColor(Constantes.COLOR_AZUL_BLANCO);
				}

				g2d.fillRect(0, 0, c.getWidth(), c.getHeight());
				g2d.dispose();
				super.paint(g, c);
			}

			@Override
			protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
				AbstractButton b = (AbstractButton) c;
				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2d.setFont(b.getFont());
				g2d.setColor(b.getForeground());
				FontMetrics fm = g2d.getFontMetrics();
				int x = textRect.x + (textRect.width - fm.stringWidth(text)) / 2;
				int y = textRect.y + fm.getAscent();
				g2d.drawString(text, x, y);
			}
		});

		panelOpcionesRespuesta.add(botonOpcion);
		panelOpcionesRespuesta.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_BOTON_OPCION)));
	}

	/**
	 * METODO REUTILIZABLE: Colorea un botón de respuesta (de opción o numérico) en
	 * verde o rojo según haya sido o no la respuesta correcta. Centraliza un
	 * if/else que antes estaba duplicado entre la botonera numérica
	 * ({@code generarBotoneraNumerica}) y los botones de opción ya respondidos
	 * ({@code mostrarPreguntaEspecifica}) dentro de MainGUI.
	 */
	public static void colorearSegunAcierto(JButton boton, boolean acierto) {
		boton.setBackground(acierto ? Constantes.COLOR_RESPUESTA_CORRECTA : Constantes.COLOR_RESPUESTA_INCORRECTA);
	}

	/**
	 * METODO REUTILIZABLE: Marca un botón (numérico o de opción) como "respondida
	 * en modo Examen" sin revelar si es correcta, con un azul más oscuro distinto
	 * del verde/rojo de acierto.
	 * 
	 * @param boton El botón (numérico o de opción) a marcar.
	 */
	public static void colorearComoMarcadaExamen(JButton boton) {
		boton.setBackground(Constantes.COLOR_RESPUESTA_EXAMEN_MARCADA);
	}

	/**
	 * METODO REUTILIZABLE: Restablece el color de fondo normal (azul) de un botón
	 * de opción ya deshabilitado que no fue ni la correcta ni la elegida por el
	 * usuario. Sin esto el botón se queda en blanco, ya que el pintado de
	 * {@link #configurarBotonOpcion} usa {@code getBackground()} cuando está
	 * deshabilitado.
	 * 
	 * @param boton El botón de opción a restablecer.
	 */
	public static void colorearNormal(JButton boton) {
		boton.setBackground(Constantes.COLOR_BOTON_NORMAL);
	}

	/**
	 * Aplica el diseño visual al panel de selección de Tests usando la misma base
	 * que el panel de Temas, pero distribuyendo los botones numéricos en una
	 * cuadrícula simétrica de máximo 10 columnas por fila, sin deformarlos.
	 *
	 * @param panelTest             El panel raíz de la pantalla de Tests.
	 * @param etiquetaSeleccionTest El título con el nombre del Tema seleccionado.
	 * @param botonVolverTemas      El botón inferior de retorno al panel de Temas.
	 * @return El panel interno donde se añadirán directamente los botones.
	 */
	public static JPanel aplicarDisenoPanelTests(JPanel panelTest, JLabel etiquetaSeleccionTest,
			JButton botonVolverTemas) {

		int anchoUniversal = Constantes.ANCHO_UNIVERSAL;

		JPanel contenedorBlanco = prepararContenedor(panelTest, Constantes.PADDING_VERTICAL_TARJETA_TEMAS,
				Constantes.PADDING_HORIZONTAL_TARJETA_TEMAS, Constantes.OPACIDAD_TARJETA, 0);

		configurarTitulo(etiquetaSeleccionTest, Constantes.ANCHO_TEXTO_TESTS, etiquetaSeleccionTest.getText());

		JPanel contenedorContenedor = new JPanel(new GridBagLayout());
		contenedorContenedor.setOpaque(false);

		JPanel listaTests = new JPanel();
		listaTests.setOpaque(false);
		listaTests.setBorder(
				new EmptyBorder(Constantes.PADDING_SUPERIOR_GRID_TESTS, Constantes.PADDING_LATERAL_GRID_TESTS,
						Constantes.PADDING_LATERAL_GRID_TESTS, Constantes.PADDING_LATERAL_GRID_TESTS));

		GridBagConstraints gbcRejilla = new GridBagConstraints();
		gbcRejilla.gridx = 0;
		gbcRejilla.gridy = 0;
		gbcRejilla.anchor = GridBagConstraints.NORTH;
		gbcRejilla.weightx = 1.0;
		gbcRejilla.weighty = 1.0;
		contenedorContenedor.add(listaTests, gbcRejilla);

		JScrollPane scrollTests = new JScrollPane(contenedorContenedor);
		scrollTests.setOpaque(false);
		scrollTests.getViewport().setOpaque(false);
		scrollTests.setAlignmentX(Component.CENTER_ALIGNMENT);
		scrollTests.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230, 150), 1));
		scrollTests.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollTests.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		Dimension dimensionRecuadro = new Dimension(escalar(Constantes.ANCHO_RECUADRO_TEMAS),
				escalar(Constantes.ALTO_RECUADRO_TEMAS));
		scrollTests.setMinimumSize(dimensionRecuadro);
		scrollTests.setPreferredSize(dimensionRecuadro);
		scrollTests.setMaximumSize(dimensionRecuadro);

		configurarBotonEnlace(botonVolverTemas, anchoUniversal, botonVolverTemas.getText());

		contenedorBlanco.add(etiquetaSeleccionTest);
		contenedorBlanco.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_MEDIO)));
		contenedorBlanco.add(scrollTests);
		contenedorBlanco.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_ESTANDAR)));
		contenedorBlanco.add(botonVolverTemas);

		MainGUI.refrescarPanel(panelTest);
		return listaTests;
	}

	/**
	 * METODO REUTILIZABLE: Configura la cuadrícula simétrica donde se reparten los
	 * botones numéricos de selección de Test dentro de un Tema. Antes
	 * {@code new GridLayout(0, 10, 30, 60)} estaba escrito directamente dentro de
	 * MainGUI; ahora usa las constantes de la cuadrícula de Tests.
	 */
	public static void configurarGridTests(JPanel contenedorGrid) {
		contenedorGrid.setLayout(new GridLayout(0, Constantes.COLUMNAS_GRID_TESTS, escalar(Constantes.HGAP_GRID_TESTS),
				escalar(Constantes.VGAP_GRID_TESTS)));
	}

	/**
	 * METODO REUTILIZABLE: Da estilo a un botón cuadrado de selección numérica,
	 * usado tanto en el grid de Tests de un Tema como en la botonera numérica del
	 * panel de Preguntas. El pintado consulta el color de fondo ya asignado (por
	 * {@link #colorearSegunProgreso} o {@link #colorearSegunAcierto}) para decidir
	 * su variante "presionado", y dibuja un marco grueso adicional cuando el botón
	 * representa la pregunta actualmente seleccionada.
	 *
	 * @param botonTest El botón cuadrado a estilizar.
	 */
	public static void configurarBotonCuadradoTest(JButton botonTest) {
		botonTest.setFont(new Font("SansSerif", Font.BOLD, escalarFuente(Constantes.TAMANO_FUENTE_BOTON_CUADRADO)));
		botonTest.setForeground(new Color(40, 45, 50));
		botonTest.setFocusPainted(false);
		botonTest.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		botonTest.setContentAreaFilled(false);
		botonTest.setOpaque(false);
		botonTest.setCursor(new Cursor(Cursor.HAND_CURSOR));

		Dimension dimBoton = escalarDimension(Constantes.ANCHO_BOTON_CUADRADO, Constantes.ALTO_BOTON_CUADRADO);
		botonTest.setMinimumSize(dimBoton);
		botonTest.setPreferredSize(dimBoton);
		botonTest.setMaximumSize(dimBoton);

		botonTest.setUI(new BasicButtonUI() {
			@Override
			public void paint(Graphics g, JComponent c) {
				AbstractButton b = (AbstractButton) c;
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				Color fondoActual = b.getBackground();
				boolean estaPresionado = b.getModel().isPressed();
				boolean estaSeleccionado = b.isSelected();

				if (fondoActual != null && fondoActual.equals(Constantes.COLOR_RESPUESTA_CORRECTA)) {
					g2d.setColor(estaPresionado || estaSeleccionado ? Constantes.COLOR_RESPUESTA_CORRECTA_PRESIONADO
							: Constantes.COLOR_RESPUESTA_CORRECTA);
				} else if (fondoActual != null && fondoActual.equals(Constantes.COLOR_RESPUESTA_INCORRECTA)) {
					g2d.setColor(estaPresionado || estaSeleccionado ? Constantes.COLOR_RESPUESTA_INCORRECTA_PRESIONADO
							: Constantes.COLOR_RESPUESTA_INCORRECTA);
				} else if (fondoActual != null && fondoActual.equals(Constantes.COLOR_RESPUESTA_EXAMEN_MARCADA)) {
					g2d.setColor(
							estaPresionado || estaSeleccionado ? Constantes.COLOR_RESPUESTA_EXAMEN_MARCADA_PRESIONADO
									: Constantes.COLOR_RESPUESTA_EXAMEN_MARCADA);
				} else if (fondoActual != null && fondoActual.equals(Constantes.COLOR_TEST_BIEN)) {
					g2d.setColor(estaPresionado || estaSeleccionado ? Constantes.COLOR_TEST_BIEN_PRESIONADO
							: Constantes.COLOR_TEST_BIEN);
				} else if (fondoActual != null && fondoActual.equals(Constantes.COLOR_TEST_REGULAR)) {
					g2d.setColor(estaPresionado || estaSeleccionado ? Constantes.COLOR_TEST_REGULAR_PRESIONADO
							: Constantes.COLOR_TEST_REGULAR);
				} else if (fondoActual != null && fondoActual.equals(Constantes.COLOR_TEST_MAL)) {
					g2d.setColor(estaPresionado || estaSeleccionado ? Constantes.COLOR_TEST_MAL_PRESIONADO
							: Constantes.COLOR_TEST_MAL);
				} else {
					g2d.setColor(estaPresionado || estaSeleccionado ? Constantes.COLOR_BOTON_PRESIONADO
							: Constantes.COLOR_AZUL_BLANCO);
				}

				g2d.fillRect(0, 0, c.getWidth(), c.getHeight());

				if (estaSeleccionado) {
					g2d.setColor(new Color(40, 45, 50));
					int grosorBorde = Constantes.GROSOR_BORDE_SELECCION;
					for (int i = 0; i < grosorBorde; i++) {
						g2d.drawRect(i, i, c.getWidth() - 1 - (i * 2), c.getHeight() - 1 - (i * 2));
					}
				} else {
					b.setForeground(new Color(40, 45, 50));
				}

				g2d.dispose();
				super.paint(g, c);
			}
		});
	}

	/**
	 * Aplica el diseño visual e interactivo al panel de inicio de sesión.
	 * 
	 * @param panelLogin         El panel raíz de la pantalla de Login.
	 * @param etiquetaUsuario    El título con el texto identificativo del campo.
	 * @param textFieldUsuario   El campo de texto donde se introduce el DNI.
	 * @param botonIniciarSesion El botón principal de inicio de sesión.
	 * @param mensajeErrorLogin  La etiqueta de aviso de error.
	 * @param botonContacto      El botón inferior de enlace de contacto/soporte.
	 */
	public static void aplicarDisenoPanelLogin(JPanel panelLogin, JLabel etiquetaUsuario, JTextField textFieldUsuario,
			JButton botonIniciarSesion, JLabel mensajeErrorLogin, JButton botonContacto) {

		int anchoUniversal = Constantes.ANCHO_UNIVERSAL;

		JPanel contenedorBlanco = prepararContenedor(panelLogin, Constantes.PADDING_VERTICAL_TARJETA,
				Constantes.PADDING_HORIZONTAL_TARJETA, Constantes.OPACIDAD_TARJETA);

		configurarTitulo(etiquetaUsuario, anchoUniversal, etiquetaUsuario.getText());
		configurarCampoTexto(textFieldUsuario, anchoUniversal);
		configurarBotonPrincipal(botonIniciarSesion, anchoUniversal);

		inicializarColoresMensaje(mensajeErrorLogin, Constantes.COLOR_TEXTO_ERROR, Constantes.COLOR_FONDO_ERROR);
		JPanel buzonMensajes = crearBuzonMensajes(anchoUniversal, mensajeErrorLogin);

		configurarBotonEnlace(botonContacto, anchoUniversal, botonContacto.getText());

		contenedorBlanco.add(etiquetaUsuario);
		contenedorBlanco.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_ESTANDAR)));
		contenedorBlanco.add(textFieldUsuario);
		contenedorBlanco.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_PEQUENO)));
		contenedorBlanco.add(botonIniciarSesion);
		contenedorBlanco.add(buzonMensajes);
		contenedorBlanco.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_ESTANDAR)));
		contenedorBlanco.add(botonContacto);

		MainGUI.refrescarPanel(panelLogin);
	}

	/**
	 * Aplica el diseño visual al panel de Resultados reutilizando la misma tarjeta
	 * que el panel de Login (mismas dimensiones y desplazamiento vertical clásico).
	 * El título cumple el rol de "Has fallado X preguntas" y el veredicto (APTO/NO
	 * APTO) se pinta en grande justo debajo, con el color que MainGUI le asigne
	 * según el resultado. El botón reutiliza exactamente el mismo estilo que el de
	 * iniciar sesión.
	 * 
	 * @param panelResultados        El panel raíz de la pantalla de Resultados.
	 * @param etiquetaNotaFinal      El título con el número de preguntas falladas.
	 * @param etiquetaVeredictoFinal El veredicto final (APTO/NO APTO).
	 * @param botonSalirResultados   El botón principal de retorno al grid de Tests.
	 */
	public static void aplicarDisenoPanelResultados(JPanel panelResultados, JLabel etiquetaNotaFinal,
			JLabel etiquetaVeredictoFinal, JButton botonSalirResultados) {

		int anchoUniversal = Constantes.ANCHO_UNIVERSAL;

		JPanel contenedorBlanco = prepararContenedor(panelResultados, Constantes.PADDING_VERTICAL_TARJETA,
				Constantes.PADDING_HORIZONTAL_TARJETA, Constantes.OPACIDAD_TARJETA);

		configurarTitulo(etiquetaNotaFinal, anchoUniversal, etiquetaNotaFinal.getText());
		configurarVeredicto(etiquetaVeredictoFinal, anchoUniversal);
		configurarBotonPrincipal(botonSalirResultados, anchoUniversal);

		contenedorBlanco.add(etiquetaNotaFinal);
		contenedorBlanco.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_MEDIO)));
		contenedorBlanco.add(etiquetaVeredictoFinal);
		contenedorBlanco.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_GRANDE)));
		contenedorBlanco.add(botonSalirResultados);

		MainGUI.refrescarPanel(panelResultados);
	}

	/**
	 * METODO REUTILIZABLE: Da estilo al veredicto final (APTO / NO APTO) en letra
	 * grande y centrada. El color lo decide MainGUI con setForeground() según el
	 * resultado.
	 */
	public static void configurarVeredicto(JLabel etiquetaVeredicto, int ancho) {
		etiquetaVeredicto.setAlignmentX(Component.CENTER_ALIGNMENT);
		etiquetaVeredicto.setFont(new Font("SansSerif", Font.BOLD, escalarFuente(Constantes.TAMANO_FUENTE_VEREDICTO)));
		etiquetaVeredicto.setHorizontalAlignment(SwingConstants.CENTER);

		Dimension dimensionVeredicto = escalarDimension(ancho, Constantes.ALTO_VEREDICTO);
		etiquetaVeredicto.setMinimumSize(dimensionVeredicto);
		etiquetaVeredicto.setPreferredSize(dimensionVeredicto);
		etiquetaVeredicto.setMaximumSize(dimensionVeredicto);
	}

	/**
	 * Aplica el diseño al panel de administración con el botón Volver abajo,
	 * centrado y estilizado como enlace.
	 * 
	 * @param panelAdmin               El panel raíz de la pantalla de
	 *                                 Administración.
	 * @param etiquetaRenovacion       El título con el texto identificativo del
	 *                                 campo.
	 * @param textFieldUsuarioRenovar  El campo de texto donde se introduce el DNI
	 *                                 del alumno.
	 * @param botonRenovarLicencia     El botón principal de renovación/creación de
	 *                                 licencia.
	 * @param mensajeErrorAdmin        La etiqueta de aviso de error.
	 * @param mensajeConfirmacionAdmin La etiqueta de aviso de confirmación.
	 * @param botonVolverInicioAdmin   El botón inferior de retorno al Login.
	 */
	public static void aplicarDisenoPanelAdmin(JPanel panelAdmin, JLabel etiquetaRenovacion,
			JTextField textFieldUsuarioRenovar, JButton botonRenovarLicencia, JLabel mensajeErrorAdmin,
			JLabel mensajeConfirmacionAdmin, JButton botonVolverInicioAdmin) {

		int anchoUniversal = Constantes.ANCHO_UNIVERSAL;

		JPanel contenedorBlanco = prepararContenedor(panelAdmin, Constantes.PADDING_VERTICAL_TARJETA,
				Constantes.PADDING_HORIZONTAL_TARJETA, Constantes.OPACIDAD_TARJETA);

		configurarTitulo(etiquetaRenovacion, anchoUniversal, etiquetaRenovacion.getText());
		configurarCampoTexto(textFieldUsuarioRenovar, anchoUniversal);
		configurarBotonPrincipal(botonRenovarLicencia, anchoUniversal);

		configurarBotonEnlace(botonVolverInicioAdmin, anchoUniversal, botonVolverInicioAdmin.getText());

		inicializarColoresMensaje(mensajeErrorAdmin, Constantes.COLOR_TEXTO_ERROR, Constantes.COLOR_FONDO_ERROR);
		inicializarColoresMensaje(mensajeConfirmacionAdmin, Constantes.COLOR_TEXTO_EXITO, Constantes.COLOR_FONDO_EXITO);

		JPanel buzonUnificado = crearBuzonMensajes(anchoUniversal, mensajeErrorAdmin, mensajeConfirmacionAdmin);

		contenedorBlanco.add(etiquetaRenovacion);
		contenedorBlanco.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_ESTANDAR)));
		contenedorBlanco.add(textFieldUsuarioRenovar);
		contenedorBlanco.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_PEQUENO)));
		contenedorBlanco.add(botonRenovarLicencia);
		contenedorBlanco.add(buzonUnificado);
		contenedorBlanco.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_ESTANDAR)));
		contenedorBlanco.add(botonVolverInicioAdmin);

		MainGUI.refrescarPanel(panelAdmin);
	}

	/**
	 * Aplica el diseño visual al panel de selección de Temas. A diferencia de
	 * Login/Admin, el número de Temas es variable, así que en vez de apilarlos
	 * directamente en la tarjeta, se meten dentro de un recuadro con scroll de
	 * altura fija: la tarjeta mantiene siempre el mismo tamaño limpio,
	 * independientemente de cuántos temas haya en la base de datos.
	 *
	 * @param panelTema             El panel raíz de la pantalla de Temas.
	 * @param etiquetaTema          El título de la pantalla de Temas.
	 * @param botonVolverInicioTema El botón inferior de retorno al Menú principal.
	 * @return El panel interno (dentro del scroll) donde MainGUI debe ir añadiendo
	 *         dinámicamente un botón por cada Tema disponible, usando
	 *         {@link #configurarBotonTema}.
	 */
	public static JPanel aplicarDisenoPanelTemas(JPanel panelTema, JLabel etiquetaTema, JButton botonVolverInicioTema) {

		int anchoUniversal = Constantes.ANCHO_UNIVERSAL;

		JPanel contenedorBlanco = prepararContenedor(panelTema, Constantes.PADDING_VERTICAL_TARJETA_TEMAS,
				Constantes.PADDING_HORIZONTAL_TARJETA_TEMAS, Constantes.OPACIDAD_TARJETA, 0);

		configurarTitulo(etiquetaTema, Constantes.ANCHO_TEXTO_TEMA, etiquetaTema.getText());

		JPanel listaTemas = new JPanel();
		listaTemas.setLayout(new BoxLayout(listaTemas, BoxLayout.Y_AXIS));
		listaTemas.setOpaque(false);
		listaTemas.setBorder(
				new EmptyBorder(Constantes.PADDING_SUPERIOR_LISTA_TEMAS, Constantes.PADDING_LATERAL_LISTA_TEMAS,
						Constantes.PADDING_LATERAL_LISTA_TEMAS, Constantes.PADDING_LATERAL_LISTA_TEMAS));

		JScrollPane scrollTemas = new JScrollPane(listaTemas);
		scrollTemas.setOpaque(false);
		scrollTemas.getViewport().setOpaque(false);
		scrollTemas.setAlignmentX(Component.CENTER_ALIGNMENT);
		scrollTemas.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230, 150), 1));
		scrollTemas.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollTemas.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		Dimension dimensionRecuadro = new Dimension(escalar(Constantes.ANCHO_RECUADRO_TEMAS),
				escalar(Constantes.ALTO_RECUADRO_TEMAS));
		scrollTemas.setMinimumSize(dimensionRecuadro);
		scrollTemas.setPreferredSize(dimensionRecuadro);
		scrollTemas.setMaximumSize(dimensionRecuadro);

		configurarBotonEnlace(botonVolverInicioTema, anchoUniversal, botonVolverInicioTema.getText());

		contenedorBlanco.add(etiquetaTema);
		contenedorBlanco.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_MEDIO)));
		contenedorBlanco.add(scrollTemas);
		contenedorBlanco.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_ESTANDAR)));
		contenedorBlanco.add(botonVolverInicioTema);

		MainGUI.refrescarPanel(panelTema);
		return listaTemas;
	}

	/**
	 * METODO REUTILIZABLE: Da estilo a un botón de Tema dentro del recuadro,
	 * reutilizando exactamente el mismo aspecto visual que los botones principales
	 * de toda la aplicación ({@link #configurarBotonPrincipal}), y lo añade ya al
	 * recuadro con el espaciado inferior necesario para que queden apilados de
	 * forma limpia.
	 */
	public static void configurarBotonTema(JButton botonTema, JPanel listaTemas, int ancho) {
		configurarBotonPrincipal(botonTema, ancho);
		listaTemas.add(botonTema);
		listaTemas.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_BOTON_TEMA)));
	}

	/**
	 * METODO REUTILIZABLE: Fija de un solo golpe el color de texto y de fondo de
	 * una etiqueta de aviso (error o éxito), antes de pasarla por
	 * {@link #configurarMensajeDinamico}.
	 *
	 * @param etiqueta   La etiqueta de aviso a colorear.
	 * @param colorTexto Color del texto del mensaje.
	 * @param colorFondo Color de fondo de la caja del mensaje.
	 */
	public static void inicializarColoresMensaje(JLabel etiqueta, Color colorTexto, Color colorFondo) {
		etiqueta.setForeground(colorTexto);
		etiqueta.setBackground(colorFondo);
	}

	/**
	 * METODO REUTILIZABLE: Configura uniformemente las etiquetas de título y fuerza
	 * su alineación central.
	 */
	public static void configurarTitulo(JLabel etiqueta, int ancho, String texto) {
		etiqueta.setText(texto);
		etiqueta.setAlignmentX(Component.CENTER_ALIGNMENT);
		Font fuenteTitulo = new Font("SansSerif", Font.BOLD, escalarFuente(Constantes.TAMANO_FUENTE_TITULO));
		etiqueta.setFont(fuenteTitulo);
		etiqueta.setForeground(Color.BLACK);
		etiqueta.setHorizontalAlignment(SwingConstants.CENTER);

		int anchoEscalado = escalar(ancho);
		FontMetrics fm = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB)
				.createGraphics().getFontMetrics(fuenteTitulo);
		int anchoTexto = fm.stringWidth(texto) + escalar(Constantes.MARGEN_EXTRA_TITULO);
		int anchoFinal = Math.max(anchoEscalado, anchoTexto);

		Dimension dimensionTitulo = new Dimension(anchoFinal, escalar(Constantes.ALTO_TITULO));
		etiqueta.setMinimumSize(dimensionTitulo);
		etiqueta.setPreferredSize(dimensionTitulo);
		etiqueta.setMaximumSize(dimensionTitulo);
	}

	/**
	 * METODO REUTILIZABLE: Configura uniformemente los campos de texto del sistema
	 * y fuerza su alineación central.
	 */
	public static void configurarCampoTexto(JTextField campoTexto, int ancho) {
		campoTexto.setAlignmentX(Component.CENTER_ALIGNMENT);
		campoTexto.setFont(new Font("SansSerif", Font.PLAIN, escalarFuente(16)));

		Dimension dimensionCampo = escalarDimension(ancho, Constantes.ALTO_BOTON_ESTANDAR);
		campoTexto.setMinimumSize(dimensionCampo);
		campoTexto.setPreferredSize(dimensionCampo);
		campoTexto.setMaximumSize(dimensionCampo);
		campoTexto.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 1),
				new EmptyBorder(0, escalar(10), 0, escalar(10))));
	}

	/**
	 * METODO REUTILIZABLE: Genera el fondo degradado y la tarjeta central
	 * decorativa adaptando su tamaño. Sobrecarga que mantiene el desplazamiento
	 * vertical clásico (90px) usado por Login y Admin.
	 */
	public static JPanel prepararContenedor(JPanel panelDestino, int paddingArribaAbajo, int paddingIzquierdaDerecha,
			int opacidadTarjeta) {
		return prepararContenedor(panelDestino, paddingArribaAbajo, paddingIzquierdaDerecha, opacidadTarjeta,
				Constantes.DESPLAZAMIENTO_TARJETA_CLASICA);
	}

	/**
	 * METODO REUTILIZABLE: Genera el fondo degradado y la tarjeta central
	 * decorativa adaptando su tamaño.
	 * 
	 * @param desplazamientoSuperior Inset inferior aplicado al centrado: a mayor
	 *                               valor, más se desplaza la tarjeta hacia arriba.
	 *                               Usa 0 para un centrado vertical real, ideal en
	 *                               tarjetas grandes como la de Temas.
	 */
	public static JPanel prepararContenedor(JPanel panelDestino, int paddingArribaAbajo, int paddingIzquierdaDerecha,
			int opacidadTarjeta, int desplazamientoSuperior) {
		panelDestino.setLayout(new GridBagLayout());

		JPanel panelFondoDegradado = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D) g;
				GradientPaint degradado = new GradientPaint(0, 0, Constantes.COLOR_DEGRADADO_INICIO, getWidth(),
						getHeight(), Constantes.COLOR_DEGRADADO_FIN);
				g2d.setPaint(degradado);
				g2d.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		panelFondoDegradado.setLayout(new GridBagLayout());
		panelFondoDegradado.setOpaque(false);

		Color colorTarjeta = new Color(255, 255, 255, opacidadTarjeta);

		JPanel contenedorBlanco = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setColor(colorTarjeta);
				g2d.fillRect(0, 0, getWidth(), getHeight());
				g2d.dispose();
			}
		};
		contenedorBlanco.setLayout(new BoxLayout(contenedorBlanco, BoxLayout.Y_AXIS));
		contenedorBlanco.setOpaque(false);
		contenedorBlanco.setAlignmentX(Component.CENTER_ALIGNMENT);
		contenedorBlanco.setBorder(
				BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 224, 230, 100), 1),
						new EmptyBorder(escalar(paddingArribaAbajo), escalar(paddingIzquierdaDerecha),
								escalar(paddingArribaAbajo), escalar(paddingIzquierdaDerecha))));

		GridBagConstraints gbcCentro = new GridBagConstraints();
		gbcCentro.gridx = 0;
		gbcCentro.gridy = 0;
		gbcCentro.anchor = GridBagConstraints.CENTER;
		gbcCentro.insets = new Insets(0, 0, escalar(desplazamientoSuperior), 0);

		panelFondoDegradado.add(contenedorBlanco, gbcCentro);

		GridBagConstraints gbcLlenar = new GridBagConstraints();
		gbcLlenar.gridx = 0;
		gbcLlenar.gridy = 0;
		gbcLlenar.weightx = 1.0;
		gbcLlenar.weighty = 1.0;
		gbcLlenar.fill = GridBagConstraints.BOTH;

		panelDestino.removeAll();
		panelDestino.add(panelFondoDegradado, gbcLlenar);

		return contenedorBlanco;
	}

	/**
	 * METODO REUTILIZABLE: Agrupa un conjunto de labels en un mismo espacio físico
	 * colapsando huecos vacíos.
	 */
	public static JPanel crearBuzonMensajes(int anchoUniversal, JLabel... etiquetas) {
		JPanel buzon = new JPanel();
		buzon.setLayout(new BoxLayout(buzon, BoxLayout.Y_AXIS));
		buzon.setOpaque(false);
		buzon.setAlignmentX(Component.CENTER_ALIGNMENT);

		for (JLabel label : etiquetas) {
			Color colorBorde = Constantes.COLOR_TEXTO_EXITO.equals(label.getForeground()) ? Constantes.COLOR_BORDE_EXITO
					: Constantes.COLOR_BORDE_ERROR;
			configurarMensajeDinamico(label, buzon, anchoUniversal, label.getForeground(), label.getBackground(),
					colorBorde);
			buzon.add(label);
		}
		return buzon;
	}

	/**
	 * METODO REUTILIZABLE: Aplica el diseño visual idéntico a todos los botones
	 * principales de las tarjetas.
	 */
	public static void configurarBotonPrincipal(JButton boton, int ancho) {
		boton.setAlignmentX(Component.CENTER_ALIGNMENT);
		boton.setFont(
				new Font(Constantes.FUENTE_BOTON_PRINCIPAL.getName(), Constantes.FUENTE_BOTON_PRINCIPAL.getStyle(),
						escalarFuente(Constantes.FUENTE_BOTON_PRINCIPAL.getSize())));

		Dimension dimensionBoton = escalarDimension(ancho, Constantes.ALTO_BOTON_ESTANDAR);
		boton.setMinimumSize(dimensionBoton);
		boton.setPreferredSize(dimensionBoton);
		boton.setMaximumSize(dimensionBoton);
		boton.setForeground(new Color(40, 45, 50));
		boton.setFocusPainted(false);
		boton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		boton.setContentAreaFilled(false);
		boton.setOpaque(false);
		boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

		boton.setUI(new BasicButtonUI() {
			@Override
			public void paint(Graphics g, JComponent c) {
				AbstractButton b = (AbstractButton) c;
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				if (!b.isEnabled()) {
					g2d.setColor(Constantes.COLOR_BOTON_DESHABILITADO);
				} else if (b.getModel().isPressed()) {
					g2d.setColor(Constantes.COLOR_BOTON_PRESIONADO);
				} else {
					g2d.setColor(Constantes.COLOR_BOTON_NORMAL);
				}

				g2d.fillRect(0, 0, c.getWidth(), c.getHeight());
				g2d.dispose();
				super.paint(g, c);
			}
		});
	}

	/**
	 * NUEVO MÉTODO MODULAR: Estiliza un botón para que actúe como un enlace de
	 * texto plano elegante y centrado.
	 */
	public static void configurarBotonEnlace(JButton boton, int ancho, String texto) {
		boton.setText(texto);
		boton.setAlignmentX(Component.CENTER_ALIGNMENT);
		boton.setFont(new Font("SansSerif", Font.PLAIN, escalarFuente(Constantes.TAMANO_FUENTE_ENLACE)));
		boton.setForeground(Constantes.COLOR_ENLACE_NORMAL);

		boton.setContentAreaFilled(false);
		boton.setBorderPainted(false);
		boton.setFocusPainted(false);
		boton.setOpaque(false);
		boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

		Dimension dimensionEnlace = escalarDimension(ancho, Constantes.ALTO_BOTON_ENLACE);
		boton.setMinimumSize(dimensionEnlace);
		boton.setPreferredSize(dimensionEnlace);
		boton.setMaximumSize(dimensionEnlace);

		boton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (boton.isEnabled()) {
					boton.setForeground(Constantes.COLOR_ENLACE_HOVER);
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (boton.isEnabled()) {
					boton.setForeground(Constantes.COLOR_ENLACE_NORMAL);
				}
			}
		});
	}

	/**
	 * Reescribe el pintado de la alerta de forma limpia. Si no hay texto,
	 * desaparece el 100% de la caja.
	 */
	public static void configurarMensajeDinamico(JLabel etiquetaMensaje, JPanel contenedorPadre, int ancho,
			Color colorTexto, Color colorFondo, Color colorBorde) {

		etiquetaMensaje.setFont(new Font("SansSerif", Font.PLAIN, escalarFuente(Constantes.TAMANO_FUENTE_MENSAJE)));
		etiquetaMensaje.setHorizontalAlignment(SwingConstants.CENTER);
		etiquetaMensaje.setForeground(colorTexto);
		etiquetaMensaje.setOpaque(false);

		Dimension dimensionMensaje = escalarDimension(ancho, Constantes.ALTO_MENSAJE);
		etiquetaMensaje.setMinimumSize(dimensionMensaje);
		etiquetaMensaje.setPreferredSize(dimensionMensaje);
		etiquetaMensaje.setMaximumSize(dimensionMensaje);
		etiquetaMensaje.setAlignmentX(Component.CENTER_ALIGNMENT);

		etiquetaMensaje.setUI(new BasicLabelUI() {
			@Override
			public void paint(Graphics g, JComponent c) {
				String txt = etiquetaMensaje.getText();
				if (txt == null || txt.trim().isEmpty() || txt.equals(" ")) {
					return;
				}

				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				int yInicio = escalar(Constantes.DESPLAZAMIENTO_VERTICAL_MENSAJE);
				int anchoEfectivo = c.getWidth() - 1;
				int altoEfectivo = c.getHeight() - yInicio - 1;

				g2d.setColor(colorFondo);
				g2d.fillRect(1, yInicio, anchoEfectivo - 1, altoEfectivo);

				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
				g2d.setColor(colorBorde);
				g2d.drawRect(1, yInicio, anchoEfectivo - 1, altoEfectivo);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				g2d.dispose();

				g.translate(0, yInicio / 2);
				super.paint(g, c);
			}
		});

		String textoInicial = etiquetaMensaje.getText();
		etiquetaMensaje.setVisible(textoInicial != null && !textoInicial.trim().isEmpty() && !textoInicial.equals(" "));

		etiquetaMensaje.addPropertyChangeListener("text", evt -> {
			String nuevoTexto = etiquetaMensaje.getText();
			boolean tieneContenido = nuevoTexto != null && !nuevoTexto.trim().isEmpty() && !nuevoTexto.equals(" ");

			if (etiquetaMensaje.isVisible() != tieneContenido) {
				etiquetaMensaje.setVisible(tieneContenido);
				contenedorPadre.revalidate();
				contenedorPadre.repaint();
			}
		});
	}

	/**
	 * METODO REUTILIZABLE: Construye el diálogo modal de selección de modo
	 * (Estudio/Examen/Repaso) mostrado al pulsar un Test en el grid, reutilizando
	 * el mismo estilo de botón principal que el resto de la app. El botón Repaso se
	 * deshabilita si "repasoDisponible" es falso.
	 *
	 * @param ventanaPadre     La ventana principal, usada como padre del diálogo.
	 * @param repasoDisponible Si existe un intento previo en modo Examen para este
	 *                         Test.
	 * @param accionEstudio    Acción a ejecutar si el alumno elige Estudio.
	 * @param accionExamen     Acción a ejecutar si el alumno elige Examen.
	 * @param accionRepaso     Acción a ejecutar si el alumno elige Repaso.
	 */
	public static void mostrarDialogoSeleccionModo(java.awt.Window ventanaPadre, boolean repasoDisponible,
			Runnable accionEstudio, Runnable accionExamen, Runnable accionRepaso) {

		javax.swing.JDialog dialogo = new javax.swing.JDialog(ventanaPadre, "Elige modo de test",
				java.awt.Dialog.ModalityType.APPLICATION_MODAL);
		dialogo.setLayout(new BoxLayout(dialogo.getContentPane(), BoxLayout.Y_AXIS));
		dialogo.getContentPane().setBackground(Color.WHITE);

		JButton botonEstudio = new JButton("Estudio");
		JButton botonExamen = new JButton("Examen");
		JButton botonRepaso = new JButton("Repaso");

		configurarBotonPrincipal(botonEstudio, Constantes.ANCHO_UNIVERSAL);
		configurarBotonPrincipal(botonExamen, Constantes.ANCHO_UNIVERSAL);
		configurarBotonPrincipal(botonRepaso, Constantes.ANCHO_UNIVERSAL);
		botonRepaso.setEnabled(repasoDisponible);

		botonEstudio.addActionListener(e -> {
			dialogo.dispose();
			accionEstudio.run();
		});
		botonExamen.addActionListener(e -> {
			dialogo.dispose();
			accionExamen.run();
		});
		botonRepaso.addActionListener(e -> {
			dialogo.dispose();
			accionRepaso.run();
		});

		JPanel contenido = new JPanel();
		contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
		contenido.setBorder(new EmptyBorder(escalar(Constantes.ESPACIADO_GRANDE), escalar(Constantes.ESPACIADO_GRANDE),
				escalar(Constantes.ESPACIADO_GRANDE), escalar(Constantes.ESPACIADO_GRANDE)));
		contenido.setBackground(Color.WHITE);
		contenido.add(botonEstudio);
		contenido.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_PEQUENO)));
		contenido.add(botonExamen);
		contenido.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_PEQUENO)));
		contenido.add(botonRepaso);

		dialogo.add(contenido);
		dialogo.pack();
		dialogo.setLocationRelativeTo(ventanaPadre);
		dialogo.setVisible(true);
	}

	/**
	 * Aplica el diseño visual al panel de fin de Examen, con la misma estructura
	 * exacta que {@link #aplicarDisenoPanelResultados} (nota + veredicto + un único
	 * botón principal), salvo que aquí el botón lleva siempre a la corrección (modo
	 * Repaso), nunca directo al grid de Tests.
	 * 
	 * @param panelFinExamen               El panel raíz de la pantalla de fin de
	 *                                     Examen.
	 * @param etiquetaNotaFinalExamen      El título con el número de preguntas
	 *                                     falladas.
	 * @param etiquetaVeredictoFinalExamen El veredicto final (APTO/NO APTO).
	 * @param botonVerCorreccion           El botón principal que lleva al modo
	 *                                     Repaso.
	 * @return El contenedor blanco de la tarjeta ya ensamblado.
	 */
	public static JPanel aplicarDisenoPanelFinExamen(JPanel panelFinExamen, JLabel etiquetaNotaFinalExamen,
			JLabel etiquetaVeredictoFinalExamen, JButton botonVerCorreccion) {

		int anchoUniversal = Constantes.ANCHO_UNIVERSAL;

		JPanel contenedorBlanco = prepararContenedor(panelFinExamen, Constantes.PADDING_VERTICAL_TARJETA,
				Constantes.PADDING_HORIZONTAL_TARJETA, Constantes.OPACIDAD_TARJETA);

		configurarTitulo(etiquetaNotaFinalExamen, anchoUniversal, etiquetaNotaFinalExamen.getText());
		configurarVeredicto(etiquetaVeredictoFinalExamen, anchoUniversal);
		configurarBotonPrincipal(botonVerCorreccion, anchoUniversal);

		contenedorBlanco.add(etiquetaNotaFinalExamen);
		contenedorBlanco.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_MEDIO)));
		contenedorBlanco.add(etiquetaVeredictoFinalExamen);
		contenedorBlanco.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_GRANDE)));
		contenedorBlanco.add(botonVerCorreccion);

		MainGUI.refrescarPanel(panelFinExamen);
		return contenedorBlanco;
	}

	/**
	 * Aplica el diseño visual al panel del menú principal, mostrado justo tras el
	 * login del alumno como paso previo a elegir cómo quiere practicar: por temas,
	 * con un test aleatorio o repasando sus errores. Reutiliza la misma tarjeta y
	 * el mismo estilo de botón principal que el resto de pantallas de la app.
	 * 
	 * @param panelMenu       El panel raíz de la pantalla del Menú principal.
	 * @param etiquetaMenu    El título de la pantalla.
	 * @param botonTemas      El botón de acceso a "Tests por temas".
	 * @param botonAleatorios El botón de acceso a "Tests aleatorios".
	 * @param botonErrores    El botón de acceso al "Test de errores".
	 * @param botonVolverMenu El botón inferior de retorno al Login.
	 */
	public static void aplicarDisenoPanelMenu(JPanel panelMenu, JLabel etiquetaMenu, JButton botonTemas,
			JButton botonAleatorios, JButton botonErrores, JButton botonVolverMenu) {

		int anchoUniversal = Constantes.ANCHO_UNIVERSAL;

		JPanel contenedorBlanco = prepararContenedor(panelMenu, Constantes.PADDING_VERTICAL_TARJETA,
				Constantes.PADDING_HORIZONTAL_TARJETA, Constantes.OPACIDAD_TARJETA);

		configurarTitulo(etiquetaMenu, anchoUniversal, etiquetaMenu.getText());
		configurarBotonPrincipal(botonTemas, anchoUniversal);
		configurarBotonPrincipal(botonAleatorios, anchoUniversal);
		configurarBotonPrincipal(botonErrores, anchoUniversal);
		configurarBotonEnlace(botonVolverMenu, anchoUniversal, botonVolverMenu.getText());

		contenedorBlanco.add(etiquetaMenu);
		contenedorBlanco.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_ESTANDAR)));
		contenedorBlanco.add(botonTemas);
		contenedorBlanco.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_PEQUENO)));
		contenedorBlanco.add(botonAleatorios);
		contenedorBlanco.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_PEQUENO)));
		contenedorBlanco.add(botonErrores);
		contenedorBlanco.add(Box.createVerticalStrut(escalar(Constantes.ESPACIADO_ESTANDAR)));
		contenedorBlanco.add(botonVolverMenu);

		MainGUI.refrescarPanel(panelMenu);
	}
}
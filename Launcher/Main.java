package launcher;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.*;

import java.awt.*;

/**
 * Lanzador y Actualizador Automático para Ahevia.
 */
public class Main {

	private static final String VERSION_URL = "https://raw.githubusercontent.com/AngelHernandeezz/public-app-tests-dgt/main/version.txt";
	private static final String EXE_URL = "https://raw.githubusercontent.com/AngelHernandeezz/public-app-tests-dgt/main/Ahevia.exe";
	private static final String JRE_ZIP_URL = "https://github.com/AngelHernandeezz/public-app-tests-dgt/releases/download/JavaSupport/jre.zip";
	public static final String LOGO = "/launcher/Logo circular.png";

	private static final Color AZUL_PRINCIPAL = new Color(202, 219, 230);
	private static final Color COLOR_TARJETA = Color.WHITE;
	private static final Color COLOR_TEXTO = new Color(44, 62, 80);
	private static final Color COLOR_BARRA_FONDO = new Color(228, 240, 250);
	private static final Color COLOR_BARRA_BORDE = new Color(41, 128, 185);

	private static JFrame ventanaCarga;
	private static JLabel etiquetaEstado;
	private static Timer timerAnimacionCoche;
	private static float posicionCoche = 0f;
	private static JPanel panelBarra;

	public static void main(String[] args) {
		try {
			String localAppData = System.getenv("LOCALAPPDATA");
			if (localAppData == null) {
				localAppData = System.getProperty("user.home") + "/AppData/Local";
			}

			Path carpetaApp = Paths.get(localAppData, "Ahevia", "AheviaApp");
			Files.createDirectories(carpetaApp);

			Path carpetaJre = carpetaApp.resolve("jre");
			Path archivoVersionLocal = carpetaApp.resolve("version.txt");
			Path archivoExeLocal = carpetaApp.resolve("Ahevia.exe");

			boolean primeraVez = !Files.exists(archivoVersionLocal);

			String localVersion = "0.0.0";
			if (Files.exists(archivoVersionLocal)) {
				localVersion = Files.readString(archivoVersionLocal).trim();
			}

			String remoteVersion = descargarTexto(VERSION_URL);
			boolean necesitaDescargarExe = primeraVez || !localVersion.equals(remoteVersion)
					|| !Files.exists(archivoExeLocal);
			boolean necesitaJre = !Files.exists(carpetaJre);
			boolean hayTrabajoQueHacer = necesitaDescargarExe || necesitaJre;

			if (hayTrabajoQueHacer) {
				if (primeraVez) {
					mostrarVentanaCarga("Instalando...");
				}

				if (necesitaDescargarExe) {
					descargarArchivo(EXE_URL, archivoExeLocal);
					Files.writeString(archivoVersionLocal, remoteVersion);
				}

				if (necesitaJre) {
					Path jreZipTemporal = carpetaApp.resolve("jre_temp.zip");
					descargarArchivo(JRE_ZIP_URL, jreZipTemporal);
					descomprimirZip(jreZipTemporal, carpetaApp);
				}

				if (primeraVez) {
					cerrarVentanaCarga();
				}
			}

			if (Files.exists(archivoExeLocal)) {
				new ProcessBuilder(archivoExeLocal.toString()).directory(carpetaApp.toFile()).start();
			} else {
				JOptionPane.showMessageDialog(null, "No se pudo encontrar el ejecutable de Ahevia.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}

		} catch (Exception e) {
			cerrarVentanaCarga();
			try {
				String localAppData = System.getenv("LOCALAPPDATA");
				Path archivoExeLocal = Paths.get(
						localAppData != null ? localAppData : System.getProperty("user.home") + "/AppData/Local",
						"AheviaApp", "Ahevia.exe");

				if (Files.exists(archivoExeLocal)) {
					new ProcessBuilder(archivoExeLocal.toString()).start();
				} else {
					JOptionPane.showMessageDialog(null, "Error al iniciar la aplicación:\n" + e.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Crea y muestra la ventana de carga como una ventana NORMAL del sistema (con
	 * su barra de título, icono y botones min/cerrar), igual que la ventana
	 * principal de Ahevia. Dentro, el mismo degradado azul y la misma tarjeta
	 * blanca redondeada que usa la app, pero con colores sólidos (sin
	 * transparencia). Incluye una barra de progreso con un coche animado que se
	 * desplaza claramente debajo de la barra, sin recortarse.
	 *
	 * @param texto Mensaje inicial a mostrar.
	 */
	private static void mostrarVentanaCarga(String texto) {
		try {

			SwingUtilities.invokeAndWait(() -> {
				ventanaCarga = new JFrame("Ahevia");
				ventanaCarga.setUndecorated(false);
				ventanaCarga.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				ventanaCarga.setResizable(false);

				try {
					URL urlLogo = Main.class.getResource(LOGO);

					if (urlLogo != null) {
						Image imgNativa = new ImageIcon(urlLogo).getImage();

						// Creamos una lista para almacenar las diferentes resoluciones del icono
						List<Image> listaIconos = new ArrayList<>();

						// Generamos las medidas exactas que pide el sistema operativo de forma limpia
						listaIconos.add(imgNativa.getScaledInstance(16, 16, Image.SCALE_SMOOTH)); // Para la esquina
																									// superior de
																									// la ventana
						listaIconos.add(imgNativa.getScaledInstance(32, 32, Image.SCALE_SMOOTH)); // Para barra de
																									// tareas
																									// estándar
						listaIconos.add(imgNativa.getScaledInstance(48, 48, Image.SCALE_SMOOTH)); // Para pantallas con
																									// escalado
																									// DPI
						listaIconos.add(imgNativa.getScaledInstance(64, 64, Image.SCALE_SMOOTH)); // Para alta
																									// resolución

						// Le pasamos la colección completa a la ventana en vez de un solo tamaño
						ventanaCarga.setIconImages(listaIconos);
					} else {
						System.err.println("No se pudo encontrar el archivo del logo en los recursos.");
					}
				} catch (Exception e) {
					System.err.println("Error al cargar y procesar los iconos de la aplicación: " + e.getMessage());
				}

				JPanel panelFondo = new JPanel(new GridBagLayout()) {
					@Override
					protected void paintComponent(Graphics g) {
						super.paintComponent(g);
						Graphics2D g2d = (Graphics2D) g.create();
						g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g2d.setPaint(AZUL_PRINCIPAL);
						g2d.fillRect(0, 0, getWidth(), getHeight());
						g2d.dispose();
					}
				};
				panelFondo.setOpaque(true);
				panelFondo.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

				JPanel tarjeta = new JPanel() {
					@Override
					protected void paintComponent(Graphics g) {
						Graphics2D g2d = (Graphics2D) g.create();
						g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g2d.setColor(COLOR_TARJETA);
						g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
						g2d.setColor(AZUL_PRINCIPAL);
						g2d.setStroke(new BasicStroke(2.2f));
						g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);
						g2d.dispose();
					}
				};
				tarjeta.setOpaque(false);
				tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
				tarjeta.setBorder(BorderFactory.createEmptyBorder(26, 32, 22, 32));

				etiquetaEstado = new JLabel(texto);
				etiquetaEstado.setForeground(COLOR_TEXTO);
				etiquetaEstado.setFont(new Font("SansSerif", Font.BOLD, 17));
				etiquetaEstado.setAlignmentX(Component.CENTER_ALIGNMENT);

				panelBarra = crearPanelBarraConCoche();
				panelBarra.setAlignmentX(Component.CENTER_ALIGNMENT);

				tarjeta.add(etiquetaEstado);
				tarjeta.add(Box.createVerticalStrut(20));
				tarjeta.add(panelBarra);

				GridBagConstraints gbc = new GridBagConstraints();
				panelFondo.add(tarjeta, gbc);

				ventanaCarga.setContentPane(panelFondo);
				ventanaCarga.setSize(400, 230);
				ventanaCarga.setLocationRelativeTo(null);
				ventanaCarga.setAlwaysOnTop(true);
				ventanaCarga.setVisible(true);
			});
		} catch (Exception ignored) {
		}

		iniciarAnimacionCoche();
	}

	/**
	 * Crea la barra de progreso y, DEBAJO de ella, el coche animado y GIRADO.
	 */
	private static JPanel crearPanelBarraConCoche() {
		JPanel panel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				int altoBarra = 10;
				int yBarra = 10;

				g2d.setColor(COLOR_BARRA_FONDO);
				g2d.fillRoundRect(0, yBarra, getWidth(), altoBarra, altoBarra, altoBarra);
				g2d.setColor(COLOR_BARRA_BORDE);
				g2d.setStroke(new BasicStroke(1.5f));
				g2d.drawRoundRect(0, yBarra, getWidth() - 1, altoBarra, altoBarra, altoBarra);

				Font fuenteCoche = new Font("SansSerif", Font.PLAIN, 28);
				g2d.setFont(fuenteCoche);
				FontMetrics fm = g2d.getFontMetrics();

				int anchoCoche = fm.stringWidth("🚗");
				int recorrido = getWidth() - anchoCoche;
				int x = (int) (posicionCoche * recorrido);
				int yCoche = yBarra + altoBarra + 10 + fm.getAscent();

				var transformacionOriginal = g2d.getTransform();

				g2d.translate(x + anchoCoche / 2, yCoche);
				g2d.scale(-1, 1);

				g2d.drawString("🚗", -anchoCoche / 2, 0);

				g2d.setTransform(transformacionOriginal);
				// ---------------------------------------------------------

				g2d.dispose();
			}
		};
		panel.setOpaque(false);
		Dimension tamanoPanel = new Dimension(310, 70);
		panel.setPreferredSize(tamanoPanel);
		panel.setMaximumSize(tamanoPanel);
		panel.setMinimumSize(tamanoPanel);
		return panel;
	}

	/**
	 * Arranca el bucle de animación del coche: recorre la barra de izquierda a
	 * derecha y vuelve a empezar, repintando solo el panel de la barra.
	 */
	private static void iniciarAnimacionCoche() {
		posicionCoche = 0f;
		timerAnimacionCoche = new Timer(20, e -> {
			posicionCoche += 0.02f;
			if (posicionCoche > 1f) {
				posicionCoche = 0f;
			}
			if (panelBarra != null) {
				panelBarra.repaint();
			}
		});
		timerAnimacionCoche.start();
	}

	/**
	 * Detiene la animación y cierra/libera la ventana de carga, justo antes de
	 * lanzar Ahevia.exe o de mostrar un error. Es seguro llamarla aunque la ventana
	 * nunca se haya llegado a mostrar (no había trabajo que hacer).
	 */
	private static void cerrarVentanaCarga() {
		if (timerAnimacionCoche != null) {
			timerAnimacionCoche.stop();
		}
		if (ventanaCarga != null) {
			SwingUtilities.invokeLater(() -> ventanaCarga.dispose());
		}
	}

	private static String descargarTexto(String url) throws IOException {
		try (var stream = URI.create(url).toURL().openStream()) {
			return new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
		}
	}

	private static void descargarArchivo(String url, Path destino) throws IOException {
		try (var stream = URI.create(url).toURL().openStream()) {
			Files.copy(stream, destino, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static void descomprimirZip(Path archivoZip, Path directorioDestino) throws IOException {
		try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(archivoZip))) {
			ZipEntry entrada;
			while ((entrada = zis.getNextEntry()) != null) {
				Path destinoFinal = directorioDestino.resolve(entrada.getName());

				if (!destinoFinal.normalize().startsWith(directorioDestino.normalize())) {
					throw new IOException("Ruta inválida en el ZIP.");
				}

				if (entrada.isDirectory()) {
					Files.createDirectories(destinoFinal);
				} else {
					Path padre = destinoFinal.getParent();
					if (padre != null) {
						Files.createDirectories(padre);
					}
					Files.copy(zis, destinoFinal, StandardCopyOption.REPLACE_EXISTING);
				}
				zis.closeEntry();
			}
		} finally {
			Files.deleteIfExists(archivoZip);
		}
	}
}
package app.modelo;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Representa a un usuario (alumno o administrador) del sistema.
 * 
 * @author Ángel Hernández
 */
public class Usuario {
	private final String dni;
	private final String nombre;
	private final boolean licenciaActiva;
	private final boolean administrador;
	private final LocalDateTime fechaLicencia;
	private final LocalDateTime fechaCaducidad;
	private final Map<String, ProgresoTest> progreso;

	/**
	 * @param dni            DNI del usuario, usado como identificador en MongoDB.
	 * @param licenciaActiva Si la licencia del usuario está actualmente activa.
	 * @param administrador  Si el usuario tiene rol de administrador.
	 * @param fechaLicencia  Fecha en que se concedió/renovó la licencia.
	 * @param fechaCaducidad Fecha en que caduca la licencia actual.
	 * @param nombre         Nombre de la autoescuela asociada al usuario.
	 * @param progreso       Progreso guardado por Test (idTest -> ProgresoTest),
	 *                       nunca {@code null} (se sustituye por un mapa vacío).
	 */
	public Usuario(String dni, boolean licenciaActiva, boolean administrador, LocalDateTime fechaLicencia,
			LocalDateTime fechaCaducidad, String nombre, Map<String, ProgresoTest> progreso) {
		this.dni = dni;
		this.nombre = nombre;
		this.licenciaActiva = licenciaActiva;
		this.administrador = administrador;
		this.fechaLicencia = fechaLicencia;
		this.fechaCaducidad = fechaCaducidad;
		this.progreso = progreso != null ? progreso : new HashMap<>();
	}

	public String getDni() {
		return dni;
	}

	public boolean isLicenciaActiva() {
		return licenciaActiva;
	}

	public boolean isAdministrador() {
		return administrador;
	}

	public LocalDateTime getFechaLicencia() {
		return fechaLicencia;
	}

	public LocalDateTime getFechaCaducidad() {
		return fechaCaducidad;
	}

	public String getNombre() {
		return nombre;
	}

	public Map<String, ProgresoTest> getProgreso() {
		return progreso;
	}

	/**
	 * Actualiza (o crea) el progreso de un Test concreto para este usuario,
	 * sobrescribiendo su entrada en {@code progreso} con los datos de la última
	 * corrección.
	 *
	 * @param idTest               Identificador del Test corregido.
	 * @param fallos               Número de preguntas falladas en esta corrección.
	 * @param idsPreguntasFalladas Conjunto actualizado de IDs de preguntas falladas
	 *                             pendientes de repaso para ese Test.
	 */
	public void actualizarProgreso(String idTest, int fallos, Set<String> idsPreguntasFalladas) {
		ProgresoTest anterior = progreso.get(idTest);
		Map<String, Integer> respuestasPrevias = anterior != null ? anterior.getRespuestasUltimoIntento() : null;
		progreso.put(idTest, new ProgresoTest(fallos, idsPreguntasFalladas, respuestasPrevias));
	}

	/**
	 * Igual que {@link #actualizarProgreso(String, int, Set)} pero además fija el
	 * último intento en modo Examen, usado tras finalizar un Examen.
	 *
	 * @param idTest                  Identificador del Test corregido.
	 * @param fallos                  Número de preguntas falladas en esta
	 *                                corrección.
	 * @param idsPreguntasFalladas    Conjunto actualizado de IDs de preguntas
	 *                                falladas pendientes de repaso para ese Test.
	 * @param respuestasUltimoIntento Respuestas del intento en modo Examen
	 *                                (idPregunta -> índice de opción elegida).
	 */
	public void actualizarProgreso(String idTest, int fallos, Set<String> idsPreguntasFalladas,
			Map<String, Integer> respuestasUltimoIntento) {
		progreso.put(idTest, new ProgresoTest(fallos, idsPreguntasFalladas, respuestasUltimoIntento));
	}
}
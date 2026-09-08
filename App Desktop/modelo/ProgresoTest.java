package app.modelo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Progreso guardado de un alumno en un Test concreto: cuántas preguntas falló
 * en su última corrección, cuáles siguen pendientes de repaso (modo "Test de
 * errores"), y qué respondió en su último intento en modo Examen (usado para
 * alimentar el modo Repaso de ESE test).
 *
 * @author Ángel Hernández
 */
public class ProgresoTest {
	private final int fallos;
	private final Set<String> idsPreguntasFalladas;
	private final Map<String, Integer> respuestasUltimoIntento;

	/**
	 * @param fallos                  Número de preguntas falladas en la última
	 *                                corrección de este Test.
	 * @param idsPreguntasFalladas    IDs de las preguntas de este Test que siguen
	 *                                pendientes de repaso.
	 * @param respuestasUltimoIntento Respuestas del último intento en modo Examen
	 *                                (idPregunta -> índice de opción elegida),
	 *                                nunca {@code null} (se sustituye por un mapa
	 *                                vacío). Ausente/vacío significa que el Test
	 *                                todavía no se ha hecho en modo Examen.
	 */
	public ProgresoTest(int fallos, Set<String> idsPreguntasFalladas, Map<String, Integer> respuestasUltimoIntento) {
		this.fallos = fallos;
		this.idsPreguntasFalladas = idsPreguntasFalladas;
		this.respuestasUltimoIntento = respuestasUltimoIntento != null ? respuestasUltimoIntento : new HashMap<>();
	}

	/**
	 * Sobrecarga de compatibilidad para llamadas que todavía no manejan respuestas
	 * de Examen (p. ej. progreso reconstruido en modo "Test de errores").
	 *
	 * @param fallos               Número de preguntas falladas en la última
	 *                             corrección de este Test.
	 * @param idsPreguntasFalladas IDs de las preguntas de este Test que siguen
	 *                             pendientes de repaso.
	 */
	public ProgresoTest(int fallos, Set<String> idsPreguntasFalladas) {
		this(fallos, idsPreguntasFalladas, new HashMap<>());
	}

	public int getFallos() {
		return fallos;
	}

	public Set<String> getIdsPreguntasFalladas() {
		return idsPreguntasFalladas;
	}

	public Map<String, Integer> getRespuestasUltimoIntento() {
		return respuestasUltimoIntento;
	}
}
package app.logica;

/**
 * Modo en el que se ejecuta el motor del examen, elegido por el alumno antes de
 * arrancar un Test desde el grid de Tests de un Tema.
 *
 * @author Ángel Hernández
 */
public enum ModoTest {
	/**
	 * Corrección al momento, explicación visible, sin cronómetro, sin poder cambiar
	 * respuesta.
	 */
	ESTUDIO,
	/** Con cronómetro, sin corrección visible, respuesta libre hasta el final. */
	EXAMEN,
	/**
	 * Solo lectura del último intento en modo Examen, sin cronómetro, sin poder
	 * cambiar respuesta.
	 */
	REPASO
}
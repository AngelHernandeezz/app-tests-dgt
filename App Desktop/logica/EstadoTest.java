package app.logica;

/**
 * Clasificación visual del progreso de un Test ya corregido, usada para
 * colorear su botón en el grid de selección de Tests de un Tema.
 *
 * @author Ángel Hernández
 */
public enum EstadoTest {
	/** El alumno todavía no ha realizado este Test. */
	NO_HECHO,
	/** Realizado sin ningún fallo. */
	BIEN,
	/** Realizado con fallos, pero dentro del margen de aprobado. */
	REGULAR,
	/** Realizado y no aprobado. */
	MAL
}
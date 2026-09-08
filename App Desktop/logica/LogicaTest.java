package app.logica;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.constantes.Constantes;
import app.modelo.Pregunta;

/**
 * Validaciones y reglas de negocio que antes vivían dentro de MainGUI:
 * normalizar/validar el DNI, corregir un test ya respondido y decidir su
 * veredicto final.
 * 
 * @author Ángel Hernández
 */
public class LogicaTest {

	/** Clase de solo métodos estáticos: no debe instanciarse. */
	private LogicaTest() {
	}

	/**
	 * Normaliza un DNI tal como lo escribe el usuario: quita espacios sobrantes y
	 * pasa a mayúsculas, igual que se guarda en la base de datos.
	 * 
	 * @param dniBruto El texto introducido por el usuario, puede ser {@code null}.
	 * @return El DNI normalizado, o cadena vacía si "dniBruto" era {@code null}.
	 */
	public static String normalizarDni(String dniBruto) {
		return dniBruto == null ? "" : dniBruto.trim().toUpperCase();
	}

	/**
	 * Comprueba si un DNI ya normalizado cumple el formato español estándar: 8
	 * cifras seguidas de 1 letra, lo que de paso garantiza la longitud esperada
	 * ({@link Constantes#LONGITUD_DNI}). No verifica que la letra sea la
	 * correspondiente al número (eso requeriría el algoritmo oficial de cálculo),
	 * solo la forma.
	 * 
	 * @param dni El DNI a validar (se espera ya normalizado).
	 * @return {@code true} si su formato es válido.
	 */
	public static boolean dniValido(String dni) {
		return dni != null && dni.matches("^[0-9]{8}[A-Z]$");
	}

	/**
	 * Corrige un test comparando las respuestas del usuario (-1 = no contestada)
	 * contra la respuesta correcta de cada pregunta.
	 * 
	 * @param preguntas         Las preguntas del test, en el mismo orden en que se
	 *                          mostraron.
	 * @param respuestasUsuario El índice de opción elegido por el usuario para cada
	 *                          pregunta (-1 si no la contestó).
	 * @return El número de preguntas falladas. El veredicto (apto/no apto) se
	 *         calcula aparte con {@link #apto(int)}.
	 */
	public static int calcularResultado(List<Pregunta> preguntas, int[] respuestasUsuario) {

		int fallos = 0;
		for (int i = 0; i < preguntas.size(); i++) {
			if (respuestasUsuario[i] != preguntas.get(i).getIndiceRespuestaCorrecta()) {
				fallos++;
			}
		}
		return fallos;
	}

	/**
	 * Decide el veredicto final del test a partir del número de fallos: apto si no
	 * se supera {@link Constantes#MAX_FALLOS_PARA_APROBAR}. Antes esta comparación
	 * estaba repetida dentro de MainGUI con el "3" escrito a mano
	 * ({@code resultado <= 3}); se centraliza aquí porque es una regla de negocio,
	 * no de interfaz, y así queda ligada a la constante en vez de a un número
	 * suelto.
	 * 
	 * @param fallos El número de preguntas falladas, obtenido de
	 *               {@link #calcularResultado}.
	 * @return {@code true} si el test se considera aprobado (apto).
	 */
	public static boolean apto(int fallos) {
		return fallos <= Constantes.MAX_FALLOS_PARA_APROBAR;
	}

	/**
	 * Traduce el número de fallos de un Test ya corregido (o {@code null} si aún no
	 * se ha hecho) al {@link EstadoTest} correspondiente, usado para colorear su
	 * botón en el grid de selección: sin hacer, perfecto, aprobado con fallos o no
	 * aprobado.
	 *
	 * @param fallos El número de fallos guardado en el progreso del alumno para ese
	 *               Test, o {@code null} si todavía no lo ha realizado.
	 * @return El {@link EstadoTest} clasificado.
	 */
	public static EstadoTest clasificarTest(Integer fallos) {
		if (fallos == null) {
			return EstadoTest.NO_HECHO;
		}
		if (fallos == 0) {
			return EstadoTest.BIEN;
		}
		if (fallos <= Constantes.MAX_FALLOS_PARA_APROBAR) {
			return EstadoTest.REGULAR;
		}
		return EstadoTest.MAL;
	}

	/**
	 * Recalcula el conjunto de preguntas falladas de un Test tras una nueva
	 * corrección: añade las que se acaban de fallar y retira las que esta vez se
	 * acertaron, partiendo del conjunto previo (que puede incluir fallos de
	 * intentos anteriores). Usado tanto al corregir un Test normal como al corregir
	 * el repaso de errores.
	 *
	 * @param previas           Conjunto de IDs de preguntas falladas en intentos
	 *                          anteriores de este Test.
	 * @param preguntas         Las preguntas del Test, en el mismo orden en que se
	 *                          mostraron.
	 * @param respuestasUsuario El índice de opción elegido por el usuario para cada
	 *                          pregunta (-1 si no la contestó).
	 * @return El conjunto actualizado de IDs de preguntas falladas.
	 */
	public static Set<String> actualizarPreguntasFalladas(Set<String> previas, List<Pregunta> preguntas,
			int[] respuestasUsuario) {
		Set<String> resultado = new HashSet<>(previas);
		for (int i = 0; i < preguntas.size(); i++) {
			Pregunta p = preguntas.get(i);
			boolean acierto = respuestasUsuario[i] == p.getIndiceRespuestaCorrecta();
			if (acierto) {
				resultado.remove(p.getId());
			} else {
				resultado.add(p.getId());
			}
		}
		return resultado;
	}
}
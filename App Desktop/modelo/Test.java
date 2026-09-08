package app.modelo;

import java.util.List;

/**
 * Un test dentro de un Tema. Guarda solo los IDs de sus Preguntas (no los
 * objetos {@link Pregunta} ya resueltos): se cargan a demanda desde
 * RepositorioBD la primera vez que se necesitan, y se cachean allí (carga
 * perezosa).
 * 
 * @author Ángel Hernández
 */
public class Test {
	private final String id;
	private final List<String> idsPreguntas;

	/**
	 * @param id           Identificador del test en MongoDB.
	 * @param idsPreguntas IDs de las Preguntas que pertenecen a este test.
	 */
	public Test(String id, List<String> idsPreguntas) {
		this.id = id;
		this.idsPreguntas = idsPreguntas;
	}

	public String getId() {
		return id;
	}

	public List<String> getIdsPreguntas() {
		return idsPreguntas;
	}
}
package app.modelo;

import java.util.List;

/**
 * Un tema del temario. Guarda solo los IDs de sus Tests (no los objetos
 * {@link Test} ya resueltos): se cargan a demanda desde RepositorioBD la
 * primera vez que se necesitan, y se cachean allí (carga perezosa).
 * 
 * @author Ángel Hernández
 */
public class Tema {
	private final String id;
	private final String titulo;
	private final List<String> idsTests;

	/**
	 * @param id       Identificador del tema en MongoDB.
	 * @param titulo   Título del tema.
	 * @param idsTests IDs de los Tests que pertenecen a este tema.
	 */
	public Tema(String id, String titulo, List<String> idsTests) {
		this.id = id;
		this.titulo = titulo;
		this.idsTests = idsTests;
	}

	public String getId() {
		return id;
	}

	public String getTitulo() {
		return titulo;
	}

	public List<String> getIdsTests() {
		return idsTests;
	}
}
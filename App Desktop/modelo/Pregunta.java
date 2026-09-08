package app.modelo;

import java.util.List;

/**
 * Una pregunta de test con sus opciones, la respuesta correcta y su
 * explicación.
 * 
 * @author Ángel Hernández
 */
public class Pregunta {
	private final String id;
	private final String enunciado;
	private final List<String> opciones;
	private final int indiceRespuestaCorrecta;
	private final String explicacion;
	private final boolean tieneImagen;

	/**
	 * @param id                      Identificador de la pregunta en MongoDB.
	 * @param enunciado               Texto de la pregunta.
	 * @param opciones                Lista de opciones de respuesta.
	 * @param indiceRespuestaCorrecta Índice (dentro de "opciones") de la respuesta
	 *                                correcta.
	 * @param explicacion             Texto explicativo que se muestra tras
	 *                                responder.
	 * @param tieneImagen             Si la pregunta tiene una imagen asociada en la
	 *                                carpeta de imágenes (nombrada con su mismo
	 *                                id).
	 */
	public Pregunta(String id, String enunciado, List<String> opciones, int indiceRespuestaCorrecta, String explicacion,
			boolean tieneImagen) {
		this.id = id;
		this.enunciado = enunciado;
		this.opciones = opciones;
		this.indiceRespuestaCorrecta = indiceRespuestaCorrecta;
		this.explicacion = explicacion;
		this.tieneImagen = tieneImagen;
	}

	public String getId() {
		return id;
	}

	public String getEnunciado() {
		return enunciado;
	}

	public List<String> getOpciones() {
		return opciones;
	}

	public int getIndiceRespuestaCorrecta() {
		return indiceRespuestaCorrecta;
	}

	public String getExplicacion() {
		return explicacion;
	}

	public boolean isTieneImagen() {
		return tieneImagen;
	}
}
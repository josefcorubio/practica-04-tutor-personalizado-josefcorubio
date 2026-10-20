package com.curso.springai.tutor.exception;

//se lanza cuando falla Qdrant u Ollama
public class ServicioNoDisponibleException extends RuntimeException {
	public ServicioNoDisponibleException(String mensaje, Throwable causa) {
		super(mensaje, causa);
	}
}

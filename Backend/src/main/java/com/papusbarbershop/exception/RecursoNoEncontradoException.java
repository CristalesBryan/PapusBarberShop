package com.papusbarbershop.exception;

/**
 * Excepción lanzada cuando un recurso no se encuentra en el sistema.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}


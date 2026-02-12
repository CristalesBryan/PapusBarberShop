package com.papusbarbershop.exception;

/**
 * Excepción lanzada cuando se intenta crear un recurso que ya existe.
 */
public class RecursoDuplicadoException extends RuntimeException {

    public RecursoDuplicadoException(String message) {
        super(message);
    }
}


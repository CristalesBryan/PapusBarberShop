package com.papusbarbershop.exception;

/**
 * Excepción lanzada cuando hay un error de validación en los datos.
 */
public class ValidacionException extends RuntimeException {

    public ValidacionException(String message) {
        super(message);
    }
}


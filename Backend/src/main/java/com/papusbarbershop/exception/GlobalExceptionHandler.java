package com.papusbarbershop.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la aplicación.
 * 
 * Esta clase centraliza el manejo de todas las excepciones que pueden
 * ocurrir en la aplicación, proporcionando respuestas HTTP consistentes
 * y estructuradas para diferentes tipos de errores.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de recursos no encontrados.
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleRecursoNoEncontrado(RecursoNoEncontradoException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja excepciones de recursos duplicados.
     */
    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleRecursoDuplicado(RecursoDuplicadoException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                LocalDateTime.now(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Maneja excepciones de validación.
     */
    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(ValidacionException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja excepciones de validación de argumentos de método.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación en los campos: " + errors.toString(),
                LocalDateTime.now(),
                request.getDescription(false)
        );
        errorResponse.setValidationErrors(errors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja excepciones de integridad de datos (duplicados, restricciones, etc.).
     * NOTA: Ya no debería haber restricción única en la tabla citas, pero mantenemos este handler
     * por si hay otras restricciones en otras tablas.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        String mensaje = "Error de integridad de datos";
        String mensajeDetallado = ex.getMessage();
        
        // Detectar si es un error de duplicado
        if (mensajeDetallado != null) {
            if (mensajeDetallado.contains("duplicate key") || mensajeDetallado.contains("llave duplicada")) {
                // Ya no debería haber restricción única en citas, pero si aparece, 
                // es un error inesperado que necesita ser investigado
                if (mensajeDetallado.contains("barbero_id") && mensajeDetallado.contains("fecha") && mensajeDetallado.contains("hora")) {
                    mensaje = "Error inesperado: Se detectó una restricción única en citas. " +
                             "Por favor, verifique que la restricción única haya sido eliminada de la base de datos. " +
                             "Nota: Las citas completadas o canceladas no deberían bloquear la creación de nuevas citas.";
                } else {
                    mensaje = "Ya existe un registro con estos datos. Por favor, verifique la información.";
                }
            } else if (mensajeDetallado.contains("foreign key")) {
                mensaje = "No se puede realizar esta operación porque hay datos relacionados.";
            } else if (mensajeDetallado.contains("not null")) {
                mensaje = "Faltan datos requeridos para completar la operación.";
            }
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                mensaje,
                LocalDateTime.now(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Maneja excepciones generales no capturadas.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor: " + ex.getMessage(),
                LocalDateTime.now(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Clase interna que representa la estructura de respuesta de error.
     */
    public static class ErrorResponse {
        private int status;
        private String message;
        private LocalDateTime timestamp;
        private String path;
        private Map<String, String> validationErrors;

        public ErrorResponse(int status, String message, LocalDateTime timestamp, String path) {
            this.status = status;
            this.message = message;
            this.timestamp = timestamp;
            this.path = path;
        }

        // Getters y Setters
        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }

        public Map<String, String> getValidationErrors() { return validationErrors; }
        public void setValidationErrors(Map<String, String> validationErrors) { this.validationErrors = validationErrors; }
    }
}


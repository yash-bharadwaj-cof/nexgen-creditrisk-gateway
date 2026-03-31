package com.nexgen.sb.creditrisk.exception;

/**
 * Thrown when incoming request fails business validation rules (CR-001 through CR-007).
 * Handled by the GlobalExceptionHandler to produce a structured error response.
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String errorCode;

    public ValidationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

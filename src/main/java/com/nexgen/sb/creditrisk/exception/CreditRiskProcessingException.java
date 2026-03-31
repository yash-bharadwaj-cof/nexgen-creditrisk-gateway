package com.nexgen.sb.creditrisk.exception;

/**
 * Thrown when an unexpected runtime error occurs during credit risk processing.
 * Wraps the original cause so callers can inspect the root failure.
 */
public class CreditRiskProcessingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CreditRiskProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

package org.example.model.exception;

/**
 * Thrown when data violates validation constraints (e.g., negative price, invalid format).
 */
public class ValidationException extends DomainException {
    public ValidationException(String message) {
        super(message);
    }
}

package org.example.model.exception;

/**
 * Thrown when an expected entity does not exist in the system.
 */
public class NotFoundException extends DomainException {
    public NotFoundException(String message) {
        super(message);
    }
}

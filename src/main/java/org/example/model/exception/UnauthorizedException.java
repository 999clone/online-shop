package org.example.model.exception;

/**
 * Thrown when an unauthenticated user or unauthorized role attempts an action.
 */
public class UnauthorizedException extends DomainException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

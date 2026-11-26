package org.example.model.exception;

/**
 * Thrown when a business logic precondition is violated
 * (e.g., modifying someone else's cart, paying already paid order, etc.).
 */
public class BusinessRuleException extends DomainException {
    public BusinessRuleException(String message) {
        super(message);
    }
}

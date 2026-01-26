package com.borsibaar.exception;

import static com.borsibaar.exception.FaultReason.NOT_FOUND;

/**
 * Exception thrown when a requested resource cannot be found.
 * Results in HTTP 404 Not Found response.
 */
public class NotFoundException extends BorsibaarClientException {

    public NotFoundException(String messageKey) {
        super(NOT_FOUND, messageKey);
    }

    public NotFoundException(String messageKey, Object[] parameters) {
        super(NOT_FOUND, messageKey, parameters);
    }

    public NotFoundException(String code, String messageKey, Object[] parameters) {
        super(code, messageKey, parameters);
    }

    // Static factory methods

    public static NotFoundException of(String messageKey, Object... parameters) {
        return new NotFoundException(messageKey, parameters);
    }

    public static NotFoundException of(String messageKey) {
        return new NotFoundException(messageKey);
    }

    /**
     * Creates a NotFoundException for an entity type and ID.
     * Example: NotFoundException.forEntity("Product", 123) -> "Product not found: 123"
     */
    public static NotFoundException forEntity(String entityType, Object id) {
        return new NotFoundException(entityType + " not found", new Object[]{id});
    }

    /**
     * Creates a NotFoundException for an entity type without ID.
     */
    public static NotFoundException forEntity(String entityType) {
        return new NotFoundException(entityType + " not found");
    }
}

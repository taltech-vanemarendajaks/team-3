package com.borsibaar.exception;

import static com.borsibaar.exception.FaultReason.DUPLICATE_RESOURCE;

/**
 * Exception thrown when attempting to create a resource that already exists.
 * Results in HTTP 409 Conflict response.
 */
public class DuplicateResourceException extends BorsibaarClientException {

    public DuplicateResourceException(String messageKey) {
        super(DUPLICATE_RESOURCE, messageKey);
    }

    public DuplicateResourceException(String messageKey, Object[] parameters) {
        super(DUPLICATE_RESOURCE, messageKey, parameters);
    }

    // Static factory methods

    public static DuplicateResourceException of(String messageKey, Object... parameters) {
        return new DuplicateResourceException(messageKey, parameters);
    }

    /**
     * Creates a DuplicateResourceException for an entity type with a duplicate name.
     * Example: DuplicateResourceException.forName("Category", "Drinks") 
     *          -> "Category with name already exists: Drinks"
     */
    public static DuplicateResourceException forName(String entityType, String name) {
        return new DuplicateResourceException(entityType + " with name already exists", new Object[]{name});
    }

    /**
     * Creates a DuplicateResourceException for a generic entity type.
     */
    public static DuplicateResourceException forEntity(String entityType) {
        return new DuplicateResourceException("A " + entityType.toLowerCase() + " with this name already exists");
    }
}

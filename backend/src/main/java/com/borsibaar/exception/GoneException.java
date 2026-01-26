package com.borsibaar.exception;

import static com.borsibaar.exception.FaultReason.RESOURCE_DELETED;

public class GoneException extends BorsibaarClientException {

    public GoneException(String messageKey) {
        super(RESOURCE_DELETED, messageKey);
    }

    public GoneException(String messageKey, Object[] parameters) {
        super(RESOURCE_DELETED, messageKey, parameters);
    }

    public static GoneException of(String messageKey, Object... parameters) {
        return new GoneException(messageKey, parameters);
    }

    public static GoneException resourceDeleted(String resourceType) {
        return new GoneException(resourceType + " has been deleted");
    }
}

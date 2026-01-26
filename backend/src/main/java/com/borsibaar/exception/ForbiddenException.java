package com.borsibaar.exception;

import static com.borsibaar.exception.FaultReason.FORBIDDEN;

public class ForbiddenException extends BorsibaarClientException {

    public ForbiddenException(String messageKey) {
        super(FORBIDDEN, messageKey);
    }

    public ForbiddenException(FaultReason code, String messageKey) {
        super(code, messageKey);
    }

    public ForbiddenException(String messageKey, Object[] parameters) {
        super(FORBIDDEN, messageKey, parameters);
    }


    public static ForbiddenException of(String messageKey, Object... parameters) {
        return new ForbiddenException(messageKey, parameters);
    }


    public static ForbiddenException adminRequired() {
        return new ForbiddenException(FaultReason.ADMIN_REQUIRED, "Admin role required");
    }
    public static ForbiddenException resourceAccessDenied(String resourceType) {
        return new ForbiddenException(resourceType + " does not belong to your organization");
    }
}

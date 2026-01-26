package com.borsibaar.exception;

import static com.borsibaar.exception.FaultReason.VALIDATION_ERROR;

public class ValidationException extends BorsibaarBusinessException {

    public ValidationException(String messageKey) {
        super(VALIDATION_ERROR.toString(), messageKey, new Object[]{});
    }

    public ValidationException(String messageKey, Object[] parameters) {
        super(VALIDATION_ERROR.toString(), messageKey, parameters);
    }

    public ValidationException(FaultReason code, String messageKey, Object[] parameters) {
        super(code.toString(), messageKey, parameters);
    }

    public static ValidationException of(String messageKey, Object... parameters) {
        return new ValidationException(messageKey, parameters);
    }

    public static ValidationException invalidField(String fieldName, String reason) {
        return new ValidationException(fieldName + ": " + reason);
    }

    public static ValidationException requiredField(String fieldName) {
        return new ValidationException(fieldName + " is required");
    }

    public static ValidationException constraintViolation(String message) {
        return new ValidationException(FaultReason.CONSTRAINT_VIOLATION, message, new Object[]{});
    }
}

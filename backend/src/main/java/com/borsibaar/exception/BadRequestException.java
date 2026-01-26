package com.borsibaar.exception;

import static com.borsibaar.exception.FaultReason.BAD_REQUEST;

/**
 * Exception thrown when a request contains invalid data.
 * Results in HTTP 400 Bad Request response.
 */
public class BadRequestException extends BorsibaarClientException {

    public BadRequestException(String messageKey) {
        super(BAD_REQUEST, messageKey);
    }

    public BadRequestException(String messageKey, Object[] parameters) {
        super(BAD_REQUEST, messageKey, parameters);
    }

    public BadRequestException(FaultReason code, String messageKey) {
        super(code, messageKey);
    }

    public BadRequestException(FaultReason code, String messageKey, Object[] parameters) {
        super(code, messageKey, parameters);
    }

    // Static factory methods

    public static BadRequestException of(String messageKey, Object... parameters) {
        return new BadRequestException(messageKey, parameters);
    }

    /**
     * Creates a BadRequestException for a blank field.
     */
    public static BadRequestException fieldBlank(String fieldName) {
        return new BadRequestException(fieldName + " must not be blank");
    }

    /**
     * Creates a BadRequestException for missing organization.
     */
    public static BadRequestException noOrganization() {
        return new BadRequestException(FaultReason.ORGANIZATION_REQUIRED, "User has no organization");
    }

    /**
     * Creates a BadRequestException for organization mismatch.
     */
    public static BadRequestException organizationMismatch(String entityType) {
        return new BadRequestException(FaultReason.ORGANIZATION_MISMATCH, 
                entityType + " does not belong to this organization");
    }

    /**
     * Creates a BadRequestException for insufficient stock.
     */
    public static BadRequestException insufficientStock(String productName) {
        return new BadRequestException(FaultReason.INSUFFICIENT_STOCK, 
                "Insufficient stock for product", new Object[]{productName});
    }
}

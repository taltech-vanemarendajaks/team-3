package com.borsibaar.exception;


public class BorsibaarClientException extends BorsibaarBusinessException {

    public BorsibaarClientException(FaultReason code, String messageKey) {
        super(code.toString(), messageKey, new Object[]{});
    }

    public BorsibaarClientException(String messageKey, Object[] parameters) {
        super(FaultReason.BAD_REQUEST.toString(), messageKey, parameters);
    }

    public BorsibaarClientException(FaultReason code, String messageKey, Object[] parameters) {
        super(code.toString(), messageKey, parameters);
    }

    public BorsibaarClientException(String code, String messageKey, Object[] parameters) {
        super(code, messageKey, parameters);
    }

    public static BorsibaarClientException of(String messageKey, Object... parameters) {
        return new BorsibaarClientException(messageKey, parameters);
    }

    public static BorsibaarClientException of(FaultReason code, String messageKey, Object... parameters) {
        return new BorsibaarClientException(code, messageKey, parameters);
    }
}

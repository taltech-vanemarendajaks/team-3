package com.borsibaar.exception;

import static com.borsibaar.exception.FaultReason.INTERNAL_SERVER_ERROR;

public class BorsibaarServerException extends BorsibaarBusinessException {

    public BorsibaarServerException(String messageKey, Object[] parameters) {
        super(INTERNAL_SERVER_ERROR.toString(), messageKey, parameters);
    }

    public static BorsibaarServerException of(String messageKey, Object... parameters) {
        return new BorsibaarServerException(messageKey, parameters);
    }

}

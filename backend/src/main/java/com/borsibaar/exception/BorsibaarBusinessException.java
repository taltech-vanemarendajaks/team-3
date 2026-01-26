package com.borsibaar.exception;

import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;


@Getter
public abstract class BorsibaarBusinessException extends RuntimeException {

    private final String code;
    private final String messageKey;
    private final Object[] parameters;

    protected BorsibaarBusinessException(String code, String messageKey, Object[] parameters) {
        super(formulateMessage(messageKey, parameters));
        this.code = code;
        this.messageKey = messageKey;
        this.parameters = parameters;
    }

    protected BorsibaarBusinessException(String code, String messageKey, Object[] parameters,
                                         Throwable cause) {
        super(formulateMessage(messageKey, parameters), cause);
        this.code = code;
        this.messageKey = messageKey;
        this.parameters = parameters;
    }

    protected BorsibaarBusinessException(String code, String messageKey, Object[] parameters,
                                         Throwable cause, boolean enableSuppression, 
                                         boolean writableStackTrace) {
        super(formulateMessage(messageKey, parameters), cause, enableSuppression, writableStackTrace);
        this.code = code;
        this.messageKey = messageKey;
        this.parameters = parameters;
    }

    private static String formulateMessage(String messageKey, Object[] parameters) {
        if (parameters == null || parameters.length == 0) {
            return messageKey;
        }

        String paramString = String.join(", ",
                Stream.of(parameters)
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .toList());

        return messageKey + ": " + paramString;
    }
}

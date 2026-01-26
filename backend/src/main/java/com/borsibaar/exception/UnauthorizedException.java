package com.borsibaar.exception;

import static com.borsibaar.exception.FaultReason.UNAUTHORIZED;

public class UnauthorizedException extends BorsibaarClientException {

    public UnauthorizedException(String messageKey) {
        super(UNAUTHORIZED, messageKey);
    }

    public UnauthorizedException(String messageKey, Object[] parameters) {
        super(UNAUTHORIZED, messageKey, parameters);
    }

    public static UnauthorizedException of(String messageKey, Object... parameters) {
        return new UnauthorizedException(messageKey, parameters);
    }

    public static UnauthorizedException notAuthenticated() {
        return new UnauthorizedException("Not authenticated");
    }

    public static UnauthorizedException invalidAuthentication() {
        return new UnauthorizedException("Invalid authentication");
    }

}

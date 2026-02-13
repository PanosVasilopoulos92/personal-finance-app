package org.viators.personalfinanceapp.exceptions;

public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException(String message) {
        super(message, ErrorCodeEnum.INVALID_CREDENTIALS);
    }

    public InvalidCredentialsException() {
        super("Invalid Credentials provided", ErrorCodeEnum.INVALID_CREDENTIALS);
    }
}

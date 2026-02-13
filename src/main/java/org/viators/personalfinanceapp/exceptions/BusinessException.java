package org.viators.personalfinanceapp.exceptions;

import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException {

    private final ErrorCodeEnum errorCode;
    private final transient Object details;

    protected BusinessException(String message, ErrorCodeEnum errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }

    protected BusinessException(String message, ErrorCodeEnum errorCode, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    protected BusinessException(String message, ErrorCodeEnum errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = null;
    }

}

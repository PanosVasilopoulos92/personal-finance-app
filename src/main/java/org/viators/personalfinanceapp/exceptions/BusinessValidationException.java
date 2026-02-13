package org.viators.personalfinanceapp.exceptions;

import java.util.Map;

public class BusinessValidationException extends BusinessException {

    public BusinessValidationException(String message) {
        super(message, ErrorCodeEnum.BUSINESS_VALIDATION_FAILED);
    }

    public BusinessValidationException(String message, Map<String, String> fieldErrors) {
        super(message, ErrorCodeEnum.BUSINESS_VALIDATION_FAILED, fieldErrors);
    }

    public BusinessValidationException(String message, ErrorCodeEnum errorCode) {
        super(message, errorCode);
    }
}

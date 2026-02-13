package org.viators.personalfinanceapp.exceptions.dto;

import org.springframework.http.HttpStatus;
import org.viators.personalfinanceapp.exceptions.ErrorCodeEnum;

import java.time.Instant;
import java.util.List;

public record ValidationErrorResponse(
        int status,
        ErrorCodeEnum errorCode,
        String message,
        String path,
        Instant timestamp,
        List<FieldError> fieldErrors
) {

    public static ValidationErrorResponse of(List<FieldError> fieldErrors, String path) {
        return new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCodeEnum.VALIDATION_FAILED,
                String.format("Validation failed for %d field(s)", fieldErrors.size()),
                path,
                Instant.now(),
                fieldErrors
        );
    }
}

package org.viators.personalfinanceapp.exceptions.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.viators.personalfinanceapp.exceptions.ErrorCodeEnum;

import java.time.Instant;
import java.util.List;

/**
 * Error response for validation failures, including per-field error details.
 */
@Schema(description = "Validation error response with field-level details")

public record ValidationErrorResponse(

        @Schema(description = "HTTP status code (always 400)", example = "400")
        int status,

        @Schema(description = "Error code", example = "VALIDATION_FAILED")
        ErrorCodeEnum errorCode,

        @Schema(description = "Summary message", example = "Validation failed for 2 field(s)")
        String message,

        @Schema(description = "Request path", example = "/api/v1/items")
        String path,

        @Schema(description = "When the error occurred")
        Instant timestamp,

        @Schema(description = "Individual field validation errors")
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

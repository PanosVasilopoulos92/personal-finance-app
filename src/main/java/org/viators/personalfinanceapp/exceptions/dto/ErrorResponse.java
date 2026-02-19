package org.viators.personalfinanceapp.exceptions.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.viators.personalfinanceapp.exceptions.ErrorCodeEnum;

import java.time.Instant;

/**
 * Standard error response returned by the GlobalExceptionHandler.
 */
@Schema(description = "Standard error response")
public record ErrorResponse(
        @Schema(description = "HTTP status code", example = "404")
        int status,

        @Schema(description = "Application-specific error code", example = "RESOURCE_NOT_FOUND")
        ErrorCodeEnum errorCode,

        @Schema(description = "Human-readable error message", example = "Item does not exist")
        String message,

        @Schema(description = "Request path that caused the error",
                example = "/api/v1/items/invalid-uuid")
        String path,

        @Schema(description = "When the error occurred")
        Instant timestamp
) {

    public static ErrorResponse of(int status, ErrorCodeEnum errorCode, String message, String path) {
        return new ErrorResponse(
                status,
                errorCode,
                message,
                path,
                Instant.now()
        );
    }
}

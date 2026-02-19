package org.viators.personalfinanceapp.exceptions.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Single field validation error")
public record FieldError(
        @Schema(description = "Field name that failed validation", example = "name")
        String field,

        @Schema(description = "Validation error message", example = "Name is required")
        String message,

        @Schema(description = "The rejected value", example = "null")
        Object rejectedValue
) {
}
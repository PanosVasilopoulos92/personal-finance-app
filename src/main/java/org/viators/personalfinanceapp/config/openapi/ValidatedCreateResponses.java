package org.viators.personalfinanceapp.config.openapi;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.viators.personalfinanceapp.exceptions.dto.ErrorResponse;
import org.viators.personalfinanceapp.exceptions.dto.ValidationErrorResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Standard error responses for creation endpoints.
 *
 * <p>Unlike {@link OwnerProtectedWriteResponses}, this does not include 403
 * because the authenticated user is always the owner of what they create.
 * However, referenced entities (store, category) might not exist.</p>
 *
 * <p>Maps to:</p>
 * <ul>
 *   <li>400 — {@code MethodArgumentNotValidException} (Bean Validation)</li>
 *   <li>404 — {@code ResourceNotFoundException} for referenced entities</li>
 * </ul>
 *
 * <p>Use on: POST (create) where referenced entities are validated</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(
                responseCode = "400",
                description = "Validation failed",
                content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Referenced resource not found (e.g., store, category)",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
})
public @interface ValidatedCreateResponses {
}

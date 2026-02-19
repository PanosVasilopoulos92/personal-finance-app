package org.viators.personalfinanceapp.config.openapi;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
 * Standard error responses for write endpoints that validate input and check ownership.
 *
 * <p>Maps to the exceptions thrown by the service layer:</p>
 * <ul>
 *   <li>400 — {@code MethodArgumentNotValidException} (Bean Validation) or
 *             {@code BusinessValidationException} (business rules like duplicate names)</li>
 *   <li>403 — {@code AccessDeniedException} via ownership check</li>
 *   <li>404 — {@code ResourceNotFoundException} when referenced entity doesn't exist</li>
 * </ul>
 *
 * <p>Use on: POST (create), PUT (update), POST /{uuid}/update-price</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(
                responseCode = "400",
                description = "Validation failed or business rule violation",
                content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))
        ),
        @ApiResponse(
                responseCode = "403",
                description = "Resource belongs to another user",
                content = @Content(
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(value = """
                                {
                                  "status": 403,
                                  "errorCode": "ACCESS_DENIED",
                                  "message": "You do not have permission to access this resource",
                                  "path": "/api/v1/items/some-uuid",
                                  "timestamp": "2026-01-01T12:00:00Z"
                                }""")
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Referenced resource not found or inactive",
                content = @Content(
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(value = """
                                {
                                  "status": 404,
                                  "errorCode": "RESOURCE_NOT_FOUND",
                                  "message": "Item does not exist",
                                  "path": "/api/v1/items/invalid-uuid",
                                  "timestamp": "2026-01-01T12:00:00Z"
                                }""")
                )
        )
})
public @interface OwnerProtectedWriteResponses {
}

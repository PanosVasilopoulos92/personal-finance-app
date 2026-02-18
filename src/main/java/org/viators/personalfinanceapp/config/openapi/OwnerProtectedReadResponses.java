package org.viators.personalfinanceapp.config.openapi;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.viators.personalfinanceapp.exceptions.dto.ErrorResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Standard error responses for endpoints that read a single owned resource.
 *
 * <p>Maps to the exceptions thrown by the service layer:</p>
 * <ul>
 *   <li>403 — {@code AccessDeniedException} via {@code Utils.loggedInUserIsOwner()}</li>
 *   <li>404 — {@code ResourceNotFoundException} when UUID doesn't match an active record</li>
 * </ul>
 *
 * <p>Use on: GET /{uuid}, DELETE /{uuid}</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(
                responseCode = "403",
                description = "Resource belongs to another user",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Resource not found or inactive",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
})
public @interface OwnerProtectedReadResponses {
}

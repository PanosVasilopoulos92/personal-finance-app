package org.viators.personalfinanceapp.config.openapi;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Adds common error responses (401, 500) to all endpoints globally.
 *
 * <p>Since every endpoint except /api/auth/** requires a JWT token,
 * the 401 response applies universally. The 500 is a safety net
 * matching the catch-all in {@code GlobalExceptionHandler}.</p>
 *
 * <p>This eliminates the need to annotate every controller method
 * with these two response codes.</p>
 */
@Configuration
public class GlobalApiResponseConfig {

    @Bean
    public GlobalOpenApiCustomizer globalOpenApiCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    operation.getResponses().addApiResponse("401",
                            new ApiResponse()
                                    .description("Missing or invalid JWT token")
                                    .content(errorContent()));

                    operation.getResponses().addApiResponse("500",
                            new ApiResponse()
                                    .description("Internal server error")
                                    .content(errorContent()));
                }));
    }


    private Content errorContent() {
        return new Content().addMediaType("application/json",
                new MediaType().schema(
                        new Schema<>().$ref("#/components/schemas/ErrorResponse")
                ));
    }
}

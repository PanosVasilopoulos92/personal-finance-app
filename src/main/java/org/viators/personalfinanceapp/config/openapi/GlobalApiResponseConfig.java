package org.viators.personalfinanceapp.config.openapi;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.viators.personalfinanceapp.exceptions.dto.ErrorResponse;

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
        return openApi -> {
            // Register ErrorResponse and its dependent schemas (e.g. ErrorCodeEnum) so that
            // the $ref in errorContent() resolves correctly in every API group spec.
            ModelConverters.getInstance()
                    .readAll(ErrorResponse.class)
                    .forEach((name, schema) -> {
                        if (openApi.getComponents() == null) {
                            openApi.components(new Components());
                        }
                        openApi.getComponents().addSchemas(name, schema);
                    });

            if (openApi.getPaths() == null) return;
            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation -> {
                        operation.getResponses().addApiResponse("401",
                                new ApiResponse()
                                        .description("Missing or invalid JWT token")
                                        .content(errorContent(401, "INVALID_CREDENTIALS",
                                                "Missing or invalid JWT token")));

                        operation.getResponses().addApiResponse("500",
                                new ApiResponse()
                                        .description("Internal server error")
                                        .content(errorContent(500, "INTERNAL_SERVER_ERROR",
                                                "An unexpected error occurred. Please try again later.")));
                    }));
        };

    }

    private Content errorContent(int status, String errorCode, String message) {
        Map<String, Object> exampleValue = new LinkedHashMap<>();
        exampleValue.put("status", status);
        exampleValue.put("errorCode", errorCode);
        exampleValue.put("message", message);
        exampleValue.put("path", "/api/v1/resource");
        exampleValue.put("timestamp", "2026-01-01T12:00:00Z");

        return new Content().addMediaType("application/json",
                new MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                        .addExamples("example", new Example().value(exampleValue)));
    }
}

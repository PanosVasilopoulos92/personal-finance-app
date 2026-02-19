package org.viators.personalfinanceapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Central OpenAPI configuration for the Personal Finance API.
 *
 * <p>Defines global metadata, JWT security scheme, and API groups
 * so each feature module gets its own section in Swagger UI.</p>
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    /**
     * Global OpenAPI definition with JWT security scheme.
     *
     * <p>The security scheme tells Swagger UI to show an "Authorize" button.
     * Once a token is entered, it's sent as {@code Authorization: Bearer <token>}
     * on every subsequent test request made from the UI.</p>
     */
    @Bean
    public OpenAPI personalFinanceAppOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Personal Finance and Price Tracking API")
                        .version("1.0.0")
                        .description("""
                                REST API for managing personal finances, tracking product prices
                                across stores, comparing prices, and managing shopping lists.
                                All endpoints except authentication require a valid JWT token.)
                                """)
                        .contact(new Contact().name("PanosV")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your JWT token (obtained from /api/v1/auth/login)")));
    }

    /**
     * Groups authentication endpoints (public — no JWT required).
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("1-authentication")
                .pathsToMatch("/api/v1/auth/**", "/api/v1/auth")
                .build();
    }

    /**
     * Groups user management endpoints.
     */
    @Bean
    public GroupedOpenApi usersApi() {
        return GroupedOpenApi.builder()
                .group("2-users")
                .pathsToMatch("/api/v1/users/**", "/api/v1/users")
                .build();
    }

    /**
     * Groups item and price tracking endpoints.
     */
    @Bean
    public GroupedOpenApi itemsApi() {
        return GroupedOpenApi.builder()
                .group("3-items")
                .pathsToMatch("/api/v1/items/**", "/api/v1/items")
                .build();
    }

    @Bean
    public GroupedOpenApi storesApi() {
        return GroupedOpenApi.builder()
                .group("4-stores")
                .pathsToMatch("/api/v1/stores/**", "/api/v1/stores")
                .build();
    }

    @Bean
    public GroupedOpenApi categoriesApi() {
        return GroupedOpenApi.builder()
                .group("5-categories")
                .pathsToMatch("/api/v1/categories/**", "/api/v1/categories")
                .build();
    }

    @Bean
    public GroupedOpenApi shoppingListsApi() {
        return GroupedOpenApi.builder()
                .group("6-shopping-lists")
                .pathsToMatch("/api/v1/shopping-lists/**", "/api/v1/shopping-lists")
                .build();
    }
}

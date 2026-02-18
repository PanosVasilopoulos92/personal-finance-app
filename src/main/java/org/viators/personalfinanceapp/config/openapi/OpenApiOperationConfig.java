package org.viators.personalfinanceapp.config.openapi;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.Arrays;

/**
 * Hides {@code @AuthenticationPrincipal} parameters from OpenAPI specs globally.
 *
 * <p>These parameters are resolved from the JWT token by Spring Security,
 * not sent by the client, so they shouldn't appear in Swagger UI.</p>
 *
 * <p>Without this, SpringDoc exposes {@code loggedInUserUuid} and
 * {@code userUuid} as visible query parameters, confusing API consumers.</p>
 */
@Configuration
public class OpenApiOperationConfig {

    @Bean
    public OperationCustomizer hideAuthenticationPrincipal() {
        return (operation, handlerMethod) -> {
            boolean hasAuthPrincipal = Arrays.stream(handlerMethod.getMethodParameters())
                    .anyMatch(p -> p.hasParameterAnnotation(AuthenticationPrincipal.class));

            if (hasAuthPrincipal && operation.getParameters() != null) {
                operation.getParameters().removeIf(param ->
                        "loggedInUserUuid".equals(param.getName())
                                || "userUuid".equals(param.getName()));
            }

            return operation;
        };
    }
}

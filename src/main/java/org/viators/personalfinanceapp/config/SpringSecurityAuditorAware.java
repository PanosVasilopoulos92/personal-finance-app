package org.viators.personalfinanceapp.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.viators.personalfinanceapp.security.UserDetailsImpl;

import java.util.Optional;

public class SpringSecurityAuditorAware implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal() instanceof String) {
            return Optional.of("system");
        }

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();

        return principal != null
                ? Optional.of(principal.getUsername())
                : Optional.empty();
    }
}

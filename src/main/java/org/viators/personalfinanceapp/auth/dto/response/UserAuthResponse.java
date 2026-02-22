package org.viators.personalfinanceapp.auth.dto.response;

import org.viators.personalfinanceapp.user.User;
import org.viators.personalfinanceapp.common.enums.UserRolesEnum;

public record UserAuthResponse(
        String token,
        String tokenType,
        String userUuid,
        String email,
        String username,
        UserRolesEnum userRole,
        Long expiresIn
) {
    public static UserAuthResponse of(String token, User user, long expiresIn) {
        return new UserAuthResponse(
                token,
                "Bearer",
                user.getUuid(),
                user.getEmail(),
                user.getUsername(),
                user.getUserRole(),
                expiresIn
        );
    }
}

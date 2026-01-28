package org.viators.personalfinanceapp.dto.user.response;

import org.viators.personalfinanceapp.model.User;

public record UserSummaryResponse(
        String uuid,
        String username,
        String fullName,
        String email,
        String status
) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(
                user.getUuid(),
                user.getUsername(),
                user.getFirstName().concat(" ").concat(user.getLastName()),
                user.getEmail(),
                user.getStatus()
        );
    }
}

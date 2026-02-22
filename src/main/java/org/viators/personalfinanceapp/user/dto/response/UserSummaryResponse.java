package org.viators.personalfinanceapp.user.dto.response;

import org.viators.personalfinanceapp.user.User;

public record UserSummaryResponse(
        String uuid,
        String username,
        String fullName,
        String email,
        String status
) {
    public static UserSummaryResponse from(User user) {
        if (user == null) {
            return null;
        }

        return new UserSummaryResponse(
                user.getUuid(),
                user.getUsername(),
                user.getFirstName().concat(" ").concat(user.getLastName()),
                user.getEmail(),
                user.getStatus()
        );
    }
}

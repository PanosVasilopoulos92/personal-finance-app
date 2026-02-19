package org.viators.personalfinanceapp.security;

import org.springframework.stereotype.Service;
import org.viators.personalfinanceapp.exceptions.AccessDeniedException;

@Service
public class OwnershipAuthorizationServiceImpl implements OwnershipAuthorizationService {

    @Override
    public void verifyOwnership(String loggedInUserUuid, String ownerUuid) {
        if (loggedInUserUuid != null && !loggedInUserUuid.equals(ownerUuid)) {
            throw new AccessDeniedException("Resource does not belong to logged in user");
        }
    }
}

package org.viators.personalfinanceapp.utils;

import org.viators.personalfinanceapp.exceptions.AccessDeniedException;

public class Utils {

    public static void loggedInUserIsOwner(String loggedInUserUuid, String ownerUuid) {
        if (loggedInUserUuid != null && !loggedInUserUuid.equals(ownerUuid)) {
            throw new AccessDeniedException("Resource does not belong to logged in user");
        }
    }
}

package org.viators.personalfinanceapp.security;

/**
 * Encapsulates resource ownership authorization logic.
 *
 * <p>Replacing the static {@code Utils.loggedInUserIsOwner()} utility
 * with an injectable service makes this concern mockable in tests,
 * overridable via Spring's dependency injection, and consistent
 * with the Dependency Inversion Principle.</p>
 */
public interface OwnershipAuthorizationService {

    /**
     * Verifies that the logged-in user is the owner of the resource.
     *
     * @param loggedInUserUuid the UUID of the currently authenticated user
     * @param ownerUuid        the UUID of the resource's owner
     * @throws org.viators.personalfinanceapp.exceptions.AccessDeniedException if the UUIDs do not match
     */
    void verifyOwnership(String loggedInUserUuid, String ownerUuid);
}

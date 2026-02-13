package org.viators.personalfinanceapp.exceptions;


/**
 * Thrown when user lacks permission to access a resource.
 * HTTP Status: 403 Forbidden
 * Usage:
 * - Accessing another user's resources
 * - Insufficient role/permissions
 * - Resource ownership violation
 */
public class AccessDeniedException extends BusinessException {

    public AccessDeniedException(String message) {
        super(message, ErrorCodeEnum.ACCESS_DENIED);
    }

    public AccessDeniedException() {
        super("You don't have permission to access this resource", ErrorCodeEnum.ACCESS_DENIED);
    }
}

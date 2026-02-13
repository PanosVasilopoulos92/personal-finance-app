package org.viators.personalfinanceapp.exceptions;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super(message, ErrorCodeEnum.RESOURCE_NOT_FOUND);
    }

    public ResourceNotFoundException(String resourceType, String identifier) {
        super(
                String.format("%s not found with uuid: %s", resourceType, identifier),
                ErrorCodeEnum.RESOURCE_NOT_FOUND
        );
    }

    public ResourceNotFoundException(String resourceType, String field, Object value) {
        super(
                String.format("%s not found with %s: %s", resourceType, field, value),
                ErrorCodeEnum.RESOURCE_NOT_FOUND
        );
    }
}

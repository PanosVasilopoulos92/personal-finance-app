package org.viators.personalfinanceapp.exceptions;

public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String resourceType, String field, Object value) {
        super(
                String.format("%s with %s '%s' already exists", resourceType, field, value),
                ErrorCodeEnum.DUPLICATE_RESOURCE
        );
    }
}

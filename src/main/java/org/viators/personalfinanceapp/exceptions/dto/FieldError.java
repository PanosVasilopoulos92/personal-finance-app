package org.viators.personalfinanceapp.exceptions.dto;

public record FieldError(
        String field,
        String message,
        Object rejectedValue) {

}


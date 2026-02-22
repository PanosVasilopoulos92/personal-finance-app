package org.viators.personalfinanceapp.category.dto.request;

import org.viators.personalfinanceapp.category.Category;

import java.util.Optional;

public record UpdateCategoryRequest(
        String newName,
        String description
) {

    public void updateFields(Category category) {
        Optional.ofNullable(newName).ifPresent(category::setName);
        Optional.ofNullable(description).ifPresent(category::setDescription);
    }
}

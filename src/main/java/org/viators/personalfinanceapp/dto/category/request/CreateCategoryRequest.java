package org.viators.personalfinanceapp.dto.category.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.viators.personalfinanceapp.model.Category;

public record CreateCategoryRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters long")
        String name,

        @Size(max = 255, message = "Description cannot be more than 255 characters long")
        String description
) {

    public Category toEntity() {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);

        return category;
    }
}

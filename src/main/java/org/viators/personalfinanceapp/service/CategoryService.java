package org.viators.personalfinanceapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.viators.personalfinanceapp.dto.category.request.CreateCategoryRequest;
import org.viators.personalfinanceapp.dto.category.response.CategorySummary;
import org.viators.personalfinanceapp.exceptions.DuplicateResourceException;
import org.viators.personalfinanceapp.exceptions.ResourceNotFoundException;
import org.viators.personalfinanceapp.model.Category;
import org.viators.personalfinanceapp.model.User;
import org.viators.personalfinanceapp.model.enums.StatusEnum;
import org.viators.personalfinanceapp.repository.CategoryRepository;
import org.viators.personalfinanceapp.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategorySummary create(String userUuid, CreateCategoryRequest request) {
        if (categoryRepository.existByNameAndUser_UuidAndStatus(userUuid, request.name(), StatusEnum.ACTIVE.getCode())) {
            throw new DuplicateResourceException("There is already one category with same name for user");
        }

        User user = userRepository.findByUuidAndStatus(userUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist or is inactive"));

        Category categoryToCreate = request.toEntity();

        categoryToCreate.addUser(user);

        categoryRepository.save(categoryToCreate);
        return CategorySummary.from(categoryToCreate);
    }



}

package org.viators.personalfinanceapp.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.viators.personalfinanceapp.category.dto.request.CreateCategoryRequest;
import org.viators.personalfinanceapp.category.dto.request.UpdateCategoryRequest;
import org.viators.personalfinanceapp.category.dto.response.CategoryDetailsResponse;
import org.viators.personalfinanceapp.category.dto.response.CategorySummaryResponse;
import org.viators.personalfinanceapp.common.enums.StatusEnum;
import org.viators.personalfinanceapp.exceptions.DuplicateResourceException;
import org.viators.personalfinanceapp.exceptions.InvalidStateException;
import org.viators.personalfinanceapp.exceptions.ResourceNotFoundException;
import org.viators.personalfinanceapp.item.Item;
import org.viators.personalfinanceapp.item.ItemService;
import org.viators.personalfinanceapp.user.User;
import org.viators.personalfinanceapp.user.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final ItemService itemService;


    public Category getActiveCategory(String categoryUuid) {
        return categoryRepository.findByUuidAndStatus(categoryUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "uuid", categoryUuid));
    }

    public CategoryDetailsResponse getCategoryWithDetails(String userUuid, String categoryUuid) {
        Category result = categoryRepository.findCategoryWithRelationships(userUuid, categoryUuid)
                .orElseThrow(() -> new ResourceNotFoundException("No category found for this currentUser with that name"));

        return CategoryDetailsResponse.from(result);
    }

    /**
     * Gets paginated items for a user.
     *
     * <p> Page.map() method transforms each Category entity to an CategorySummaryResponse DTO
     * while preserving all pagination metadata (total elements, page info, etc.).</p>
     *
     * @param userUuid the owner's ID
     * @param pageable pagination parameters
     * @return page of category DTOs
     */
    public Page<CategorySummaryResponse> getCategories(String userUuid, Pageable pageable) {
        return categoryRepository.findByUser_Uuid(userUuid, pageable)
                .map(CategorySummaryResponse::from);
    }

    @Transactional
    public CategorySummaryResponse create(String userUuid, CreateCategoryRequest request) {
        if (categoryRepository.existsByNameAndUser_UuidAndStatus(request.name(), userUuid, StatusEnum.ACTIVE.getCode())) {
            throw new DuplicateResourceException("Category", "name", request.name());
        }

        User user = userService.findActiveUser(userUuid);
        Category categoryToCreate = request.toEntity();
        categoryToCreate.addUser(user);

        categoryRepository.save(categoryToCreate);
        return CategorySummaryResponse.from(categoryToCreate);
    }

    @Transactional
    public CategorySummaryResponse update(String userUuid, String categoryUuid, UpdateCategoryRequest request) {
        Category categoryToUpdate = categoryRepository.findByUuidAndUser_UuidAndStatus(categoryUuid, userUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("No category found with this uuid for this currentUser"));

        if (categoryRepository.checkAvailabilityOfNameForUpdateCategory(userUuid, StatusEnum.ACTIVE.getCode(), request.newName())) {
            throw new DuplicateResourceException("Category", "name", request.newName());
        }

        request.updateFields(categoryToUpdate);
        return CategorySummaryResponse.from(categoryToUpdate);
    }

    @Transactional
    public void archiveCategory(String uuid) {
        Category categoryToArchive = categoryRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("No category exist with this uuid"));

        categoryToArchive.setStatus(StatusEnum.INACTIVE.getCode());
    }

    @Transactional
    public CategoryDetailsResponse addItem(String userUuid, String categoryUuid, String itemUuid) {
        Category category = categoryRepository.findByUuidAndUser_Uuid(categoryUuid, userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("No category exist with this uuid"));

        Item item = itemService.getItemByUuidAndUser(itemUuid, userUuid);

        if (category.getItems().contains(item)) {
            throw  new DuplicateResourceException("Category already contains this item");
        }

        category.addItem(item);
        return CategoryDetailsResponse.from(category);
    }

    public CategoryDetailsResponse removeItem(String userUuid, String categoryUuid, String itemUuid) {
        Category category = categoryRepository.findByUuidAndUser_Uuid(categoryUuid, userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("No category exist with this uuid"));

        Item item = itemService.getItemByUuidAndUser(itemUuid, userUuid);

        if (!category.getItems().contains(item)) {
            throw  new InvalidStateException("Item does not exists in this category");
        }

        category.removeItem(item);

        return CategoryDetailsResponse.from(category);
    }

}

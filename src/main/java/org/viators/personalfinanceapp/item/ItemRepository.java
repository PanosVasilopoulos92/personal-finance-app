package org.viators.personalfinanceapp.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.viators.personalfinanceapp.category.Category;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    Optional<Item> findByUuidAndStatus(String uuid, String status);

    Optional<Item> findByUuidAndUser_Uuid(String itemUuid, String userUuid);

    Page<Item> findAllByUser_UuidAndStatus(String userUuid, String status, Pageable pageable);

    boolean existsByUuidAndStatus(String uuid, String status);

    boolean existsByNameAndUser_IdAndStatusAndCategoriesContaining(String name, Long userId,
                                                                      String status, Category category);

    boolean existsByNameAndUser_IdAndStatusAndCategoriesIsEmpty(String name, Long userId, String status);
}

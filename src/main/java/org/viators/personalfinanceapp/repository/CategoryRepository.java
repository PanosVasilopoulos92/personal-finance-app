package org.viators.personalfinanceapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.viators.personalfinanceapp.model.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByUuid(String uuid);

    Optional<Category> findByNameAndUser_UuidAndStatus(String name, String uuid, String status);

    boolean existByNameAndUser_UuidAndStatus(String name, String uuid, String status);

}

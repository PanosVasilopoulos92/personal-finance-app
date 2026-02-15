package org.viators.personalfinanceapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.viators.personalfinanceapp.model.Store;

import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByUuidAndStatusAndUserIsNullOrUser_Uuid(String uuid, String status, String userUuid);

    Optional<Store> findByNameAndStatusAndUserIsNullOrUser_Uuid(String name, String status, String userUuid);

    Optional<Store> findByUuidAndStatus(String uuid, String status);

    Optional<Store> findByUuidAndStatusAndUserIsNotNull(String uuid, String status);

    Optional<Store> findByUuidAndStatusAndUserIsNull(String uuid, String status);

    @Query("""
            select s from Store s
            where s.status = :status
            and (s.user is null or s.user.uuid = :userUuid)
            """)
    Page<Store> findAllAvailableStoresForUser(@Param("status") String status,
                                              @Param("userUuid") String userUuid,
                                              Pageable pageable);

    boolean existsByNameIgnoreCaseAndStatusAndUserIsNotNullAndUser_Uuid(String name, String status, String userUuid);
}

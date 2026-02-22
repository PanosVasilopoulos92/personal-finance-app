package org.viators.personalfinanceapp.store;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    @Query("""
            select s from Store s
            where s.uuid = :uuid
            and s.status = :status
            and (s.user is null or s.user.uuid = :userUuid)
            """)
    Optional<Store> findByUuidAndStatusAndUserIsNullOrUser_Uuid(@Param("uuid") String uuid,
                                                                @Param("status") String status,
                                                                @Param("userUuid") String userUuid);

    @Query("""
            select s from Store s
            where s.name = :name
            and s.status = :status
            and (s.user is null or s.user.uuid = :userUuid)
            """)
    Optional<Store> findByNameAndStatusAndUserIsNullOrUser_Uuid(@Param("name") String name,
                                                                @Param("status") String status,
                                                                @Param("userUuid") String userUuid);

    Optional<Store> findByUuidAndStatus(String uuid, String status);

    Optional<Store> findByUuid(String uuid);

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

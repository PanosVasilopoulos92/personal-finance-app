package org.viators.personalfinanceapp.shoppinglist;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.viators.personalfinanceapp.shoppinglist.dto.response.ShoppingListSummaryResponse;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {

    Optional<ShoppingList> findByUuidAndStatus(String uuid, String status);

    Page<ShoppingList> findAllByUser_UuidAndStatus(String userUuid, String status, Pageable pageable);

    @Query(value = """
            select sl from ShoppingList sl
            left join fetch sl.shoppingListItems
            where sl.uuid = :slUuid
            and sl.status = :slStatus""")
    Optional<ShoppingList> findByUuidAndStatusWithShopListItems(String uuid, String status);

    @Query("""
            SELECT new org.viators.personalfinanceapp.shoppinglist.dto.response.ShoppingListSummaryResponse(
                sl.uuid, sl.name, sl.description, sl.isFavorite, SIZE(sl.shoppingListItems)
            )
            FROM ShoppingList sl
            WHERE sl.user.uuid = :userUuid AND sl.status = :status
            """)
    List<ShoppingListSummaryResponse> findAllSummariesByUserUuidAndStatus(
            @Param("userUuid") String userUuid,
            @Param("status") String status);

}

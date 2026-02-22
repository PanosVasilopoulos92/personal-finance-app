package org.viators.personalfinanceapp.shoppinglistitem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.viators.personalfinanceapp.shoppinglistitem.dto.response.ShoppingListItemSummaryResponse;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {

    Optional<ShoppingListItem> findByUuidAndStatus(String uuid, String status);

    @Query("""
            SELECT new org.viators.personalfinanceapp.shoppinglistitem.dto.response.ShoppingListItemSummaryResponse(
                sli.uuid,
                sli.item.uuid,
                sli.item.name,
                sli.item.brand,
                sli.item.itemUnit,
                sli.store.uuid,
                sli.store.name,
                sli.quantity,
                sli.isPurchased,
                sli.purchasedPrice,
                sli.purchasedDate
            )
            FROM ShoppingListItem sli
            JOIN sli.item
            JOIN sli.store
            WHERE sli.uuid = :uuid AND sli.status = :status
            """)
    Optional<ShoppingListItemSummaryResponse> findShoppingListItemWithRelations(
            @Param("uuid") String uuid,
            @Param("status") String status);

    @Query("""
        select sli.id from ShoppingListItem sli
        join sli.shoppingList sl
        where sl.uuid = :slUuid
        and sli.shoppingList.uuid = :slUuid
""")
    public List<Long> findAllByShoppingList(String slUuid);
}

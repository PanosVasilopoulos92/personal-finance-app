package org.viators.personalfinanceapp.item;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.viators.personalfinanceapp.common.enums.ItemUnitEnum;
import org.viators.personalfinanceapp.common.enums.StatusEnum;

import java.time.Instant;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemSpecs {

    public static Specification<Item> belongsToUser(String loggedInUserUuid) {
        return (root, query, cb) ->
                cb.equal(root.get("user").get("uuid"), loggedInUserUuid);
    }

    public static Specification<Item> isActive() {
        return (root, query, cb) ->
                cb.equal(root.get("status"), StatusEnum.ACTIVE.getCode());
    }

    public static Specification<Item> hasStatus(StatusEnum status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status.getCode());
    }

    public static Specification<Item> nameContains(String keyword) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%".concat(keyword.toLowerCase().concat("%")));
    }

    public static Specification<Item> hasBrand(String brand) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("brand"), brand);
    }

    public static Specification<Item> hasUnit(ItemUnitEnum itemUnit) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("itemUnit"), itemUnit);
    }

    public static Specification<Item> isFavorite() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isFavorite"), true);
    }

    public static Specification<Item> inCategory(String categoryUuid) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("category").get("uuid"), categoryUuid);
    }

    public static Specification<Item> createdAfter(Instant createdAfter) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdAfter);
    }

    public static Specification<Item> createdBefore(Instant createdBefore) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdBefore);
    }


}

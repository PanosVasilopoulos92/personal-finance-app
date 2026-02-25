package org.viators.personalfinanceapp.store;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.viators.personalfinanceapp.common.enums.StatusEnum;
import org.viators.personalfinanceapp.common.enums.StoreTypeEnum;

import jakarta.persistence.criteria.Predicate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreSpecs {

    public static Specification<Store> belongsToUser(String userUuid) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user").get("uuid"), userUuid);
    }

    public static Specification<Store> hasStoreType(StoreTypeEnum storeType) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("storeType"), storeType);
    }

    public static Specification<Store> inCity(String city) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("city"), city);
    }

    public static Specification<Store> inCountry(String country) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("country"), country);
    }

    public static Specification<Store> nameContaining(String nameKeyword) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                        "%".concat(nameKeyword.toLowerCase()).concat("%"));
    }

    public static Specification<Store> hasWebsite() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isNotNull(root.get("website"));
    }

    public static Specification<Store> hasStatus(StatusEnum status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Store> locationContains(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%".concat(keyword.toLowerCase()).concat("%");
            Predicate cityMatches = cb.like(cb.lower(root.get("city")), pattern);
            Predicate regionMatches = cb.like(cb.lower(root.get("region")), pattern);
            Predicate countryMatches = cb.like(cb.lower(root.get("country")), pattern);
            Predicate addressMatches = cb.like(cb.lower(root.get("address")), pattern);
            return cb.or(cityMatches, regionMatches, countryMatches, addressMatches);
        };
    }

}

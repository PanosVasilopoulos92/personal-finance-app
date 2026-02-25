package org.viators.personalfinanceapp.priceobservation;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.viators.personalfinanceapp.common.enums.CurrencyEnum;
import org.viators.personalfinanceapp.common.enums.StoreTypeEnum;
import org.viators.personalfinanceapp.store.Store;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PriceObservationSpecs {

    private PriceObservationSpecs() {}

    public static Specification<PriceObservation> hasItemUuid(String itemUuid) {
        return ((root, query, cd) ->
                cd.equal(root.get("item").get("uuid"), itemUuid));
    }

    public static Specification<PriceObservation> dateFrom(LocalDate dateFrom) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("observationDate"), dateFrom);
    }

    public static Specification<PriceObservation> dateTo(LocalDate dateTo) {
        return ((root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("observationDate"), dateTo));
    }

    public static Specification<PriceObservation> hasStoreUuid(String storeUuid) {
        return (root, query, cb) ->
                cb.equal(root.get("store").get("uuid"), storeUuid);
    }

    public static Specification<PriceObservation> hasStoreType(StoreTypeEnum storeType) {
        return (root, query, cb) ->
                cb.equal(root.get("store").get("storeType"), storeType);
    }

    public static Specification<PriceObservation> hasStoreName(String storeName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("store").get("name"), storeName);
    }

    public static Specification<PriceObservation> hasStoreNameExplicit(String storeName) {
        return (root, query, criteriaBuilder) -> {
            // Will return PriceObservations even if store is null
            Join<PriceObservation, Store> storeJoin = root.join("store", JoinType.LEFT);
            return criteriaBuilder.equal(storeJoin.get("name"), storeName);
        };
    }

    public static Specification<PriceObservation> inCity(String city) {
        return (root, query, cb) ->
                cb.equal(root.get("store").get("city"), city);
    }

    public static Specification<PriceObservation> hasCurrency(CurrencyEnum currency) {
        return (root, query, cb) ->
                cb.equal(root.get("currency"), currency);
    }

    public static Specification<PriceObservation> hasMinPrice(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<PriceObservation> priceBetween(BigDecimal startsPrice, BigDecimal endPrice) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("price"), startsPrice, endPrice);
    }

}

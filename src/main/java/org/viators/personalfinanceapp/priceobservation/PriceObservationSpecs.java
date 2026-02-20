package org.viators.personalfinanceapp.priceobservation;

import org.springframework.data.jpa.domain.Specification;

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

    public static Specification<PriceObservation> inCity(String city) {
        return (root, query, cb) ->
                cb.equal(root.get("store").get("city"), city);
    }

    public static Specification<PriceObservation> hasCurrency(CurrencyEnum currency) {
        return (root, query, cb) ->
                cb.equal(root.get("currency"), currency);
    }

}

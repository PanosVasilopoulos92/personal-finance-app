package org.viators.personalfinanceapp.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import org.viators.personalfinanceapp.model.PriceObservation;

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
}

package org.viators.personalfinanceapp.repository.specification;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.viators.personalfinanceapp.model.Item;
import org.viators.personalfinanceapp.model.PriceObservation;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemSpecs {

    public static Specification<Item> hasUuid(String itemUuid) {
        return (root, query, cb) ->
                cb.equal(root.get("uuid"), itemUuid);
    }

    public static Specification<Item> hasPriceBetween(BigDecimal from, BigDecimal to) {
        return (root, query, cb) ->
                cb.between(getOrCreatePriceObservationJoin(root).get("price"), from, to);
    }

    @SuppressWarnings("unchecked")
    private static Join<Item, PriceObservation> getOrCreatePriceObservationJoin(From<?, Item> root) {
        return (Join<Item, PriceObservation>) root.getJoins().stream()
                .filter(j -> j.getAttribute().getName().equals("priceObservations"))
                .findFirst()
                .orElseGet(() -> root.join("priceObservations", JoinType.LEFT));
    }
}

package org.viators.personalfinanceapp.priceobservation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.viators.personalfinanceapp.common.BaseEntity;
import org.viators.personalfinanceapp.item.Item;
import org.viators.personalfinanceapp.store.Store;
import org.viators.personalfinanceapp.common.enums.CurrencyEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "price_observations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PriceObservation extends BaseEntity {

    @Column(name = "price", nullable = false, updatable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, updatable = false)
    private CurrencyEnum currency;

    @Column(name = "observation_date", nullable = false, updatable = false)
    private LocalDate observationDate;

    @Column(name = "location", nullable = false, updatable = false)
    private String location; // city or region

    @Column(name = "notes", length = 400)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false, updatable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false, updatable = false)
    private Store store;
}

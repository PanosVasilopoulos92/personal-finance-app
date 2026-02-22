package org.viators.personalfinanceapp.priceobservation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.viators.personalfinanceapp.common.enums.CurrencyEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceObservationRepository extends JpaRepository<PriceObservation, Long>,
        JpaSpecificationExecutor<PriceObservation> {

    @Override
    @EntityGraph(attributePaths = {"item", "store"})
    Page<PriceObservation> findAll(Specification<PriceObservation> spec, Pageable pageable);

    @Query("""
            select po from PriceObservation po
            where po.item.uuid = :uuid
            and po.status = :status
            order by po.createdAt desc
            limit 1
            """)
    Optional<PriceObservation> findLastActivePriceObservation(@Param("uuid") String uuid,
                                                              @Param("status") String status);

    @Query("""
            select po from PriceObservation po
            where po.item.uuid = :itemUuid
            and po.currency = :currency
            and po.observationDate between :startDate and :endDate
            order by po.observationDate asc
            """)
    List<PriceObservation> findPriceObsForInflationCalc(@Param("itemUuid") String itemUuid,
                                                        @Param("currency") CurrencyEnum currency,
                                                        @Param("startDate")LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);
}

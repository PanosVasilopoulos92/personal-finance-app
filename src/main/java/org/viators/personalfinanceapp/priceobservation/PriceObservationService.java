package org.viators.personalfinanceapp.priceobservation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.viators.personalfinanceapp.common.enums.CurrencyEnum;
import org.viators.personalfinanceapp.common.enums.StatusEnum;
import org.viators.personalfinanceapp.exceptions.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceObservationService {

    private final PriceObservationRepository priceObservationRepository;

    public PriceObservation getItemLatestPriceObservation(String itemUuid) {
        return priceObservationRepository.findLastActivePriceObservation(itemUuid, StatusEnum.ACTIVE.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("No active price found for this item"));
    }

    public Page<PriceObservation> getPriceObservationsBasedOnDateRange(Specification<PriceObservation> specs, Pageable pageable) {
        return priceObservationRepository.findAll(specs, pageable);
    }

    public List<PriceObservation> getAllPricesForItemBasedOnCurrencyAndDateRange(String itemUuid,
                                                                                 CurrencyEnum currency,
                                                                                 LocalDate startDate,
                                                                                 LocalDate endDate) {

        return priceObservationRepository.getAllPricesForItemBasedOnCurrencyAndDateRange(itemUuid, currency, startDate, endDate);
    }

}

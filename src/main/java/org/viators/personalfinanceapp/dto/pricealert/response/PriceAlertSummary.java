package org.viators.personalfinanceapp.dto.pricealert.response;

import org.viators.personalfinanceapp.model.PriceAlert;
import org.viators.personalfinanceapp.model.enums.AlertTypeEnum;

import java.time.LocalDateTime;
import java.util.List;

public record PriceAlertSummary(
        AlertTypeEnum alertType,
        LocalDateTime lastTriggeredAt,
        String itemName
) {
    public static PriceAlertSummary from(PriceAlert priceAlert) {
        return new PriceAlertSummary(
                priceAlert.getAlertType(),
                priceAlert.getLastTriggeredAt(),
                priceAlert.getItem().getName()
        );
    }

    public static List<PriceAlertSummary> listOfSummaries(List<PriceAlert> priceAlerts) {
        return priceAlerts.stream()
                .map(PriceAlertSummary::from)
                .toList();
    }
}

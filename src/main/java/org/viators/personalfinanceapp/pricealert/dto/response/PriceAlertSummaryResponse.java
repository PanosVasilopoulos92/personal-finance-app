package org.viators.personalfinanceapp.pricealert.dto.response;

import org.viators.personalfinanceapp.pricealert.PriceAlert;
import org.viators.personalfinanceapp.common.enums.AlertTypeEnum;

import java.time.LocalDateTime;
import java.util.List;

public record PriceAlertSummaryResponse(
        AlertTypeEnum alertType,
        LocalDateTime lastTriggeredAt,
        String itemName
) {
    public static PriceAlertSummaryResponse from(PriceAlert priceAlert) {
        return new PriceAlertSummaryResponse(
                priceAlert.getAlertType(),
                priceAlert.getLastTriggeredAt(),
                priceAlert.getItem().getName()
        );
    }

    public static List<PriceAlertSummaryResponse> listOfSummaries(List<PriceAlert> priceAlerts) {
        return priceAlerts.stream()
                .map(PriceAlertSummaryResponse::from)
                .toList();
    }
}

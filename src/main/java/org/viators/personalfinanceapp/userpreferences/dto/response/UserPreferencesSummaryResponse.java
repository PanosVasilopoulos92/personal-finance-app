package org.viators.personalfinanceapp.userpreferences.dto.response;

import org.viators.personalfinanceapp.store.dto.response.StoreSummaryResponse;
import org.viators.personalfinanceapp.userpreferences.UserPreferences;
import org.viators.personalfinanceapp.common.enums.CurrencyEnum;

import java.util.Set;

public record UserPreferencesSummaryResponse(
        CurrencyEnum currency,
        String location,
        Boolean notificationEnabled,
        Boolean emailAlerts,
        Set<StoreSummaryResponse> preferredStoreIds
) {
    public static UserPreferencesSummaryResponse from(UserPreferences userPreferences) {
        return new UserPreferencesSummaryResponse(
                userPreferences.getCurrency(),
                userPreferences.getLocation(),
                userPreferences.getNotificationEnabled(),
                userPreferences.getEmailAlerts(),
                StoreSummaryResponse.fromList(userPreferences.getPreferredStores())
        );
    }
}
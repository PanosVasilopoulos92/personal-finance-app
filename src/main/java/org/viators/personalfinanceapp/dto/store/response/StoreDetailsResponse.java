package org.viators.personalfinanceapp.dto.store.response;

import org.viators.personalfinanceapp.dto.user.response.UserSummaryResponse;
import org.viators.personalfinanceapp.model.Store;
import org.viators.personalfinanceapp.model.enums.StoreTypeEnum;

public record StoreDetailsResponse(
        String name,
        StoreTypeEnum storeType,
        String address,
        String city,
        String region,
        String country,
        String website,
        UserSummaryResponse userSummaryResponse
) {

    public static StoreDetailsResponse from(Store store) {
        return new StoreDetailsResponse(
                store.getName(),
                store.getStoreType(),
                store.getAddress(),
                store.getCity(),
                store.getRegion(),
                store.getCountry(),
                store.getWebsite(),
                UserSummaryResponse.from(store.getUser())
        );
    }
}

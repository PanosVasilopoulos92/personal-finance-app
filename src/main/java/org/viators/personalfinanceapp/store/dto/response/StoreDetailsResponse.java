package org.viators.personalfinanceapp.store.dto.response;

import org.viators.personalfinanceapp.user.dto.response.UserSummaryResponse;
import org.viators.personalfinanceapp.store.Store;
import org.viators.personalfinanceapp.common.enums.StoreTypeEnum;

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

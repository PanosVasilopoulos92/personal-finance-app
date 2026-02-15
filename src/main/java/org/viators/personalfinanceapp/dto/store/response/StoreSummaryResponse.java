package org.viators.personalfinanceapp.dto.store.response;

import org.viators.personalfinanceapp.model.Store;
import org.viators.personalfinanceapp.model.enums.StoreTypeEnum;

import java.util.Set;
import java.util.stream.Collectors;

public record StoreSummaryResponse(
        String uuid,
        String name,
        StoreTypeEnum storeType
) {
    public static StoreSummaryResponse from(Store store) {
        return new StoreSummaryResponse(
                store.getUuid(),
                store.getName(),
                store.getStoreType());
    }

    public static Set<StoreSummaryResponse> fromList(Set<Store> stores) {
        return stores.stream()
                .map(StoreSummaryResponse::from)
                .collect(Collectors.toSet());
    }
}

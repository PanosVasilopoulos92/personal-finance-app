package org.viators.personalfinanceapp.store.dto.request;

import org.viators.personalfinanceapp.common.enums.StatusEnum;
import org.viators.personalfinanceapp.common.enums.StoreTypeEnum;

public record StoreFilterRequest(
        StoreTypeEnum storeType,
        String inCity,
        String inCountry,
        String nameContains,
        Boolean hasWebsite,
        StatusEnum status,
        String locationContains
) {
}

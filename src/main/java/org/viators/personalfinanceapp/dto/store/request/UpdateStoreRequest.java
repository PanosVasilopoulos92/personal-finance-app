package org.viators.personalfinanceapp.dto.store.request;

import jakarta.validation.constraints.Size;
import org.viators.personalfinanceapp.model.Store;
import org.viators.personalfinanceapp.model.enums.StoreTypeEnum;

import java.util.Optional;

public record UpdateStoreRequest(
        @Size(min = 3, max = 50, message = "Name must be between 3-50 characters long")
        String name,

        StoreTypeEnum storeType,

        @Size(min = 3, max = 50, message = "Address must be between 3-50 characters long")
        String address,

        @Size(min = 3, max = 50, message = "City must be between 3-50 characters long")
        String city,

        @Size(min = 3, max = 50, message = "Region must be between 3-50 characters long")
        String region,

        @Size(min = 3, max = 50, message = "Country must be between 3-50 characters long")
        String country,

        @Size(min = 3, max = 50, message = "Website must be between 3-50 characters long")
        String website
) {

    public void updateStore(Store store) {
        Optional.ofNullable(name).ifPresent(store::setName);
        Optional.ofNullable(storeType).ifPresent(store::setStoreType);
        Optional.ofNullable(address).ifPresent(store::setAddress);
        Optional.ofNullable(city).ifPresent(store::setCity);
        Optional.ofNullable(region).ifPresent(store::setRegion);
        Optional.ofNullable(country).ifPresent(store::setCountry);
        Optional.ofNullable(website).ifPresent(store::setWebsite);
    }
}

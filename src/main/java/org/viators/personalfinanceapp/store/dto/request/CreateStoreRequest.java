package org.viators.personalfinanceapp.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.viators.personalfinanceapp.store.Store;
import org.viators.personalfinanceapp.common.enums.StoreTypeEnum;

import java.util.Optional;

public record CreateStoreRequest(
        @NotBlank(message = "Store's name is required")
        @Size(min = 3, max = 50, message = "Name must be between 3-50 characters long")
        String name,

        @NotNull(message = "Store type is required")
        StoreTypeEnum storeType,

        @NotBlank(message = "Address is required")
        @Size(min = 3, max = 50, message = "Address must be between 3-50 characters long")
        String address,

        @NotBlank(message = "City is required")
        @Size(min = 3, max = 50, message = "City must be between 3-50 characters long")
        String city,

        @Size(min = 3, max = 50, message = "Region must be between 3-50 characters long")
        String region,

        @NotBlank(message = "Country is required")
        @Size(min = 3, max = 50, message = "Country must be between 3-50 characters long")
        String country,

        @Size(min = 3, max = 50, message = "Website must be between 3-50 characters long")
        String website
) {

    public Store toEntity() {
        Store store = new Store();
        store.setName(name);
        store.setStoreType(storeType);
        store.setAddress(address);
        store.setCity(city);
        store.setCountry(country);
        Optional.ofNullable(region).ifPresent(store::setRegion);
        Optional.ofNullable(website).ifPresent(store::setWebsite);

        return store;
    }
}

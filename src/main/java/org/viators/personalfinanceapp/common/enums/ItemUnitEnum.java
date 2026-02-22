package org.viators.personalfinanceapp.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemUnitEnum {
    LITER("Liter"),
    KILOGRAM("Kg"),
    PIECE("piece");

    private final String description;
}

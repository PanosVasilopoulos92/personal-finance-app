package org.viators.personalfinanceapp.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CurrencyEnum {
    EUR("Euro", "€"),
    USD("US Dollar", "$");

    private final String description;
    private final String symbol;
}

package org.viators.personalfinanceapp.model.enums;

import lombok.Getter;

@Getter
public enum StatusEnum {
    ACTIVE("1", "Active"),
    INACTIVE("0", "Inactive");

    private final String code;
    private final String description;

    StatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static StatusEnum getStatusFromCode(String code) {
        return switch (code) {
            case "1" -> StatusEnum.ACTIVE;
            case "0" -> StatusEnum.INACTIVE;
            default -> throw new IllegalArgumentException("Not valid status code");
        };
    }

}

package com.example.inventory.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
/**
 * Represents the warehouse field options.
 */
public enum WarehouseField {
    ID("id", "Warehouse ID"),
    CODE("code", "Warehouse code"),
    NAME("name", "Warehouse name"),
    ADDRESS("address", "Address"),
    CITY("city", "City"),
    STATE("state", "State"),
    POSTAL_CODE("postalCode", "Postal code"),
    ACTIVE("active", "Active");

    private final String value;
    private final String label;

    @JsonCreator
    public static WarehouseField fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (WarehouseField field : values()) {
            if (field.name().equalsIgnoreCase(value.trim())
                    || field.value.equalsIgnoreCase(value.trim())) {
                return field;
            }
        }
        throw new IllegalArgumentException("Unsupported warehouse field: " + value);
    }
}

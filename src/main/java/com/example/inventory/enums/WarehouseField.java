package com.example.inventory.enums;

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
    POSTAL_CODE("postalCode", "Postal code");

    private final String value;
    private final String label;
}

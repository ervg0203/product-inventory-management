package com.example.inventory.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum SearchOperator {
    EQUALS, NOT_EQUALS, CONTAINS, STARTS_WITH, ENDS_WITH,
    GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL,
    IN, BETWEEN

    ;

    @JsonCreator
    public static SearchOperator fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (SearchOperator operator : values()) {
            if (operator.name().equalsIgnoreCase(value.trim())) {
                return operator;
            }
        }
        throw new IllegalArgumentException("Unsupported search operator: " + value);
    }
}

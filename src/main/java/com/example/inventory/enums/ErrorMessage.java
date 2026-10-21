package com.example.inventory.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
/**
 * Represents the error message options.
 */
public enum ErrorMessage {

    REQUIRED("%s is required"),
    NOT_FOUND("%s does not exist"),
    TOO_LONG("%s is too long"),
    MUST_BE_POSITIVE("%s must be greater than zero"),
    NEGATIVE("%s cannot be negative"),
    TOO_HIGH("%s cannot exceed %s"),
    SCALE("%s must have at most %s decimal places");

    private final String value;

    /**
     * Returns the result of the format operation.
     */
    public String format(Object... arguments) {
        return String.format(value, arguments);
    }
}

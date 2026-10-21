package com.example.inventory.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
/**
 * Represents the product field options.
 */
public enum ProductField {

    ID("id", "Product ID"),
    SKU("sku", "SKU"),
    TITLE("title", "Product title"),
    DESCRIPTION("description", "Description"),
    CATEGORY("category", "Category"),
    PRICE("price", "Price"),
    DISCOUNT_PERCENTAGE("discountPercentage", "Discount percentage"),
    RATING("rating", "Rating"),
    BRAND("brand", "Brand"),
    RETURN_POLICY("returnPolicy", "Return policy"),
    CREATED_AT("createdAt", "Created at"),
    UPDATED_AT("updatedAt", "Updated at");

    private final String value;
    private final String label;

    @JsonCreator
    /**
     * Returns the result of the from value operation.
     */
    public static ProductField fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (ProductField field : values()) {
            if (field.name().equalsIgnoreCase(value.trim())
                    || field.value.equalsIgnoreCase(value.trim())) {
                return field;
            }
        }
        throw new IllegalArgumentException("Unsupported product field: " + value);
    }
}

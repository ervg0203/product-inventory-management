package com.example.inventory.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
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
    RETURN_POLICY("returnPolicy", "Return policy");

    private final String value;
    private final String label;
}

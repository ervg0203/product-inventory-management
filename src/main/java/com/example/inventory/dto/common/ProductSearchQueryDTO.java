package com.example.inventory.dto.common;

import lombok.Getter;
import lombok.Setter;

/**
 * Query parameters accepted by the user-friendly product search endpoint.
 */
@Getter
@Setter
public class ProductSearchQueryDTO {

    private String query;
    private String title;
    private String description;
    private String sku;
    private String brand;
    private String category;
    private String returnPolicy;
    private String minPrice;
    private String maxPrice;
    private String minRating;
    private String maxRating;
    private String minDiscountPercentage;
    private String maxDiscountPercentage;
    private String priceOperator;
    private String priceValue;
    private String ratingOperator;
    private String ratingValue;
    private String discountOperator;
    private String discountValue;
    private String brands;
    private String categories;
    private String priceBetween;
    private String ratingBetween;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}

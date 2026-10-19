package com.example.inventory.dto.request;

import com.example.inventory.util.Errors;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    private String id;

    private String sku;

    private String title;

    private String description;

    private String category;

    private BigDecimal price;

    private BigDecimal discountPercentage;

    private BigDecimal rating;

    private String brand;

    private String returnPolicy;

    @Builder.Default
    private List<Errors> errors = new ArrayList<>();

    @Builder.Default
    private Boolean isValid = true;
}
package com.example.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private String id;

    private String sku;

    private String title;

    private String description;

    private String category;

    private BigDecimal price;

    private BigDecimal discountPercentage;

    private BigDecimal rating;

    private Integer stock;

    private String brand;

    private String availabilityStatus;

    private String returnPolicy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
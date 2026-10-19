package com.example.inventory.dto.response;

import com.example.inventory.util.Errors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    private String brand;

    private String returnPolicy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<Errors> errors;

    private boolean isValid;
}
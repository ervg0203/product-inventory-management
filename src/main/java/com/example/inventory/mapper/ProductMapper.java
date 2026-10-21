package com.example.inventory.mapper;

import com.example.inventory.dto.request.ProductRequestDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.entity.ProductEntity;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Component
/**
 * Provides the product mapper component.
 */
public class ProductMapper {

    /**
     * Returns the result of the to entity operation.
     */
    public ProductEntity toEntity(ProductRequestDTO requestDTO) {

        LocalDateTime now = LocalDateTime.now();

        return ProductEntity.builder()
                .sku(requestDTO.getSku())
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .category(requestDTO.getCategory())
                .price(requestDTO.getPrice())
                .discountPercentage(requestDTO.getDiscountPercentage())
                .rating(requestDTO.getRating())
                .brand(requestDTO.getBrand())
                .returnPolicy(requestDTO.getReturnPolicy())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Performs the update entity operation.
     */
    public void updateEntity(ProductEntity product, ProductRequestDTO requestDTO) {

        product.setSku(requestDTO.getSku());
        product.setTitle(requestDTO.getTitle());
        product.setDescription(requestDTO.getDescription());
        product.setCategory(requestDTO.getCategory());
        product.setPrice(requestDTO.getPrice());
        product.setDiscountPercentage(requestDTO.getDiscountPercentage());
        product.setRating(requestDTO.getRating());
        product.setBrand(requestDTO.getBrand());
        product.setReturnPolicy(requestDTO.getReturnPolicy());
        product.setUpdatedAt(LocalDateTime.now());

    }

    /**
     * Returns the result of the to response dto operation.
     */
    public ProductResponseDTO toResponseDTO(ProductEntity product) {

        return ProductResponseDTO.builder()
                .id(product.getId())
                .sku(product.getSku())
                .title(product.getTitle())
                .description(product.getDescription())
                .category(product.getCategory())
                .price(product.getPrice())
                .discountPercentage(product.getDiscountPercentage())
                .rating(product.getRating())
                .brand(product.getBrand())
                .returnPolicy(product.getReturnPolicy())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .errors(new ArrayList<>())
                .isValid(true)
                .build();
    }

    /**
     * Returns the result of the to response dto operation.
     */
    public ProductResponseDTO toResponseDTO(ProductRequestDTO product) {

        return ProductResponseDTO.builder()
                .id(product.getId())
                .sku(product.getSku())
                .title(product.getTitle())
                .description(product.getDescription())
                .category(product.getCategory())
                .price(product.getPrice())
                .discountPercentage(product.getDiscountPercentage())
                .rating(product.getRating())
                .brand(product.getBrand())
                .returnPolicy(product.getReturnPolicy())
                .errors(product.getErrors())
                .isValid(product.getIsValid())
                .build();
    }

}

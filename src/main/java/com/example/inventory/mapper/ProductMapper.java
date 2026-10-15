package com.example.inventory.mapper;

import com.example.inventory.dto.request.ProductRequestDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.entity.ProductEntity;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Component
public class ProductMapper {

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
                .stock(requestDTO.getStock())
                .brand(requestDTO.getBrand())
                .availabilityStatus(requestDTO.getAvailabilityStatus())
                .returnPolicy(requestDTO.getReturnPolicy())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void updateEntity(ProductEntity product, ProductRequestDTO requestDTO) {

        product.setSku(requestDTO.getSku());
        product.setTitle(requestDTO.getTitle());
        product.setDescription(requestDTO.getDescription());
        product.setCategory(requestDTO.getCategory());
        product.setPrice(requestDTO.getPrice());
        product.setDiscountPercentage(requestDTO.getDiscountPercentage());
        product.setRating(requestDTO.getRating());
        product.setStock(requestDTO.getStock());
        product.setBrand(requestDTO.getBrand());
        product.setAvailabilityStatus(requestDTO.getAvailabilityStatus());
        product.setReturnPolicy(requestDTO.getReturnPolicy());
        product.setUpdatedAt(LocalDateTime.now());

    }

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
                .stock(product.getStock())
                .brand(product.getBrand())
                .availabilityStatus(product.getAvailabilityStatus())
                .returnPolicy(product.getReturnPolicy())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .errors(new ArrayList<>())
                .isValid(true)
                .build();
    }

    public ProductResponseDTO toResponseDTO(ProductRequestDTO product) {

        return ProductResponseDTO.builder()
                .sku(product.getSku())
                .title(product.getTitle())
                .description(product.getDescription())
                .category(product.getCategory())
                .price(product.getPrice())
                .discountPercentage(product.getDiscountPercentage())
                .rating(product.getRating())
                .stock(product.getStock())
                .brand(product.getBrand())
                .availabilityStatus(product.getAvailabilityStatus())
                .returnPolicy(product.getReturnPolicy())
                .errors(product.getErrors())
                .isValid(product.getIsValid())
                .build();
    }

}

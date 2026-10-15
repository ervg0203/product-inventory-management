package com.example.inventory.service.impl;

import com.example.inventory.dto.request.CreateProductRequestDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.entity.ProductEntity;
import com.example.inventory.exception.ProductValidationException;
import com.example.inventory.mapper.ProductMapper;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.service.ProductService;
import com.example.inventory.util.ProductValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductValidator productValidator;

    @Override
    public ProductResponseDTO createProduct(CreateProductRequestDTO createProductRequestDTO) {
        return createProducts(List.of(createProductRequestDTO)).getFirst();
    }

    @Override
    public List<ProductResponseDTO> createProducts(
            List<CreateProductRequestDTO> productRequestDTOs) {

        if (productRequestDTOs == null || productRequestDTOs.isEmpty()) {
            throw new IllegalArgumentException("At least one product is required");
        }
        if (productRequestDTOs.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Product entries cannot be null");
        }

        productRequestDTOs.forEach(productValidator::validate);

        List<ProductResponseDTO> invalidProducts = productRequestDTOs.stream()
                .filter(product -> !Boolean.TRUE.equals(product.getIsValid()))
                .map(productMapper::toResponseDTO)
                .toList();

        if (!invalidProducts.isEmpty()) {
            throw new ProductValidationException(invalidProducts);
        }

        List<ProductEntity> productEntities = productRequestDTOs.stream()
                .map(productMapper::toEntity)
                .toList();

        List<ProductResponseDTO> productResponseDTOs;

        try {
            productResponseDTOs = productRepository.saveAll(productEntities).stream()
                    .map(productMapper::toResponseDTO)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save products: " + e.getMessage(), e);
        }

        return productResponseDTOs;
    }
}

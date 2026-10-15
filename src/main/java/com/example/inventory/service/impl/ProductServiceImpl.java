package com.example.inventory.service.impl;

import com.example.inventory.dto.request.ProductRequestDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.dto.response.ProductPageResponseDTO;
import com.example.inventory.entity.ProductEntity;
import com.example.inventory.exception.ProductNotFoundException;
import com.example.inventory.exception.ProductValidationException;
import com.example.inventory.mapper.ProductMapper;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.service.ProductService;
import com.example.inventory.util.ProductValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductValidator productValidator;

    @Override
    public ProductPageResponseDTO getAllProducts(int page, int size) {

        if (page < 0 || size < 1) {
            throw new IllegalArgumentException("Page must be zero or greater and size must be greater than zero");
        }

        Page<ProductEntity> productPage = productRepository.findAll(PageRequest.of(page, size));

        return ProductPageResponseDTO.builder()
                .products(productPage.getContent().stream()
                .map(productMapper::toResponseDTO)
                .toList())
                .totalRecords(productPage.getTotalElements())
                .page(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalPages(productPage.getTotalPages())
                .build();
    }

    @Override
    public ProductResponseDTO getProductById(String id) {
        ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return productMapper.toResponseDTO(productEntity);
    }

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {
        return createProducts(List.of(productRequestDTO)).getFirst();
    }

    @Override
    public List<ProductResponseDTO> createProducts(
            List<ProductRequestDTO> productRequestDTOs) {

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

        return productRepository.saveAll(productEntities).stream()
                .map(productMapper::toResponseDTO)
                .toList();
    }
}

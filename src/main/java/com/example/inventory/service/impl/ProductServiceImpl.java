package com.example.inventory.service.impl;

import com.example.inventory.dto.request.ProductRequestDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.dto.common.PageResponseDTO;
import com.example.inventory.config.PaginationProperties;
import com.example.inventory.entity.ProductEntity;
import com.example.inventory.exception.ProductNotFoundException;
import com.example.inventory.exception.ProductValidationException;
import com.example.inventory.mapper.ProductMapper;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.service.ProductService;
import com.example.inventory.util.ProductValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductValidator productValidator;
    private final PaginationProperties paginationProperties;

    @Override
    public PageResponseDTO<ProductResponseDTO> getAllProducts(int page, int size) {

        if (page < 0 || size < 1 || size > paginationProperties.getMaxPageSize()) {
            throw new IllegalArgumentException(
                    "Page must be zero or greater and size must be between 1 and "
                            + paginationProperties.getMaxPageSize());
        }

        Page<ProductEntity> productPage = productRepository.findAll(PageRequest.of(page, size));
        log.info("Fetched products page={}, size={}, totalRecords={}",
                page, size, productPage.getTotalElements());

        return PageResponseDTO.<ProductResponseDTO>builder()
                .content(productPage.getContent().stream()
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
        log.info("Fetched product id={}", id);
        return productMapper.toResponseDTO(productEntity);
    }

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {
        return createProducts(List.of(productRequestDTO)).getFirst();
    }

    @Override
    public List<ProductResponseDTO> createProducts(
            List<ProductRequestDTO> productRequestDTOs) {

        validateProductRequests(productRequestDTOs);

        productRequestDTOs.forEach(productValidator::validate);
        throwIfInvalid(productRequestDTOs);

        List<ProductEntity> productEntities = productRequestDTOs.stream()
                .map(productMapper::toEntity)
                .toList();

        List<ProductResponseDTO> savedProducts = productRepository.saveAll(productEntities).stream()
                .map(productMapper::toResponseDTO)
                .toList();
        log.info("Created {} products", savedProducts.size());
        return savedProducts;
    }

    @Override
    public ProductResponseDTO updateProduct(ProductRequestDTO productRequestDTOS) {
        return updateProducts(List.of(productRequestDTOS)).getFirst();
    }

    @Override
    public List<ProductResponseDTO> updateProducts(List<ProductRequestDTO> productRequestDTOS) {

        validateProductRequests(productRequestDTOS);

        Set<String> productIds = productRequestDTOS.stream()
                .map(ProductRequestDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, ProductEntity> existingProducts = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, product -> product));

        Set<String> existingProductIds = existingProducts.keySet();

        productRequestDTOS.forEach(product ->
                productValidator.validateForUpdate(product, existingProductIds));
        throwIfInvalid(productRequestDTOS);

        productRequestDTOS.forEach(product ->
                productMapper.updateEntity(existingProducts.get(product.getId()), product));

        List<ProductEntity> productEntities = productRequestDTOS.stream()
                .map(product -> existingProducts.get(product.getId()))
                .toList();

        List<ProductResponseDTO> updatedProducts = productRepository.saveAll(productEntities).stream()
                .map(productMapper::toResponseDTO)
                .toList();

        log.info("Updated {} products", updatedProducts.size());
        return updatedProducts;
    }

    private void validateProductRequests(List<ProductRequestDTO> products) {
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("At least one product is required");
        }
        if (products.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Product entries cannot be null");
        }
    }

    private void throwIfInvalid(List<ProductRequestDTO> products) {
        List<ProductResponseDTO> invalidProducts = products.stream()
                .filter(product -> !Boolean.TRUE.equals(product.getIsValid()))
                .map(productMapper::toResponseDTO)
                .toList();

        if (!invalidProducts.isEmpty()) {
            log.info("Product validation failed for {} of {} products",
                    invalidProducts.size(), products.size());
            throw new ProductValidationException(invalidProducts);
        }
    }

    @Override
    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
        log.info("Deleted product id={}", id);
    }

    @Override
    public void deleteAllProducts() {
        productRepository.deleteAll();
        log.info("Deleted all products");
    }
}

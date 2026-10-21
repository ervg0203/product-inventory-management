package com.example.inventory.service.impl;

import com.example.inventory.dto.request.ProductRequestDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.dto.common.PageResponseDTO;
import com.example.inventory.config.PaginationProperties;
import com.example.inventory.config.ImportProperties;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import com.example.inventory.dto.request.ProductSearchRequestDTO;
import com.example.inventory.util.SearchQueryBuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * Provides the product service impl component.
 */
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductValidator productValidator;
    private final PaginationProperties paginationProperties;
    private final MongoTemplate mongoTemplate;
    private final SearchQueryBuilder searchQueryBuilder;
    private final ImportProperties importProperties;

    @Override
    /**
     * Returns the result of the search products operation.
     */
    public PageResponseDTO<ProductResponseDTO> searchProducts(ProductSearchRequestDTO request) {
        if (request == null) throw new IllegalArgumentException("Search request cannot be null");
        int page = request.getPage() == null ? paginationProperties.getDefaultPage() : request.getPage();
        int size = request.getSize() == null ? paginationProperties.getDefaultPageSize() : request.getSize();
        validatePage(page, size);

        Query query = searchQueryBuilder.build(request);
        long total = mongoTemplate.count(Query.of(query), ProductEntity.class);
        query.skip((long) page * size).limit(size);
        List<ProductResponseDTO> content = mongoTemplate.find(query, ProductEntity.class).stream()
                .map(productMapper::toResponseDTO).toList();
        int totalPages = (int) Math.ceil((double) total / size);
        return PageResponseDTO.<ProductResponseDTO>builder().content(content).totalRecords(total)
                .page(page).pageSize(size).totalPages(totalPages)
                .first(page == 0).last(totalPages == 0 || page >= totalPages - 1).build();
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > paginationProperties.getMaxPageSize()) {
            throw new IllegalArgumentException("Page must be zero or greater and size must be between 1 and "
                    + paginationProperties.getMaxPageSize());
        }
    }

    @Override
    /**
     * Returns the result of the get all products operation.
     */
    public PageResponseDTO<ProductResponseDTO> getAllProducts(int page, int size) {

        validatePage(page, size);

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
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .build();
    }

    @Override
    /**
     * Returns the result of the get product by id operation.
     */
    public ProductResponseDTO getProductById(String id) {
        ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        log.info("Fetched product id={}", id);
        return productMapper.toResponseDTO(productEntity);
    }

    @Override
    /**
     * Returns the result of the create product operation.
     */
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {
        return createProducts(List.of(productRequestDTO)).getFirst();
    }

    @Override
    /**
     * Provides the create products operation.
     */
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
    /**
     * Returns the result of the update product operation.
     */
    public ProductResponseDTO updateProduct(ProductRequestDTO productRequestDTOS) {
        return updateProducts(List.of(productRequestDTOS)).getFirst();
    }

    @Override
    /**
     * Returns the result of the update products operation.
     */
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

    @Override
    public List<ProductResponseDTO> importProducts(List<ProductRequestDTO> products) {
        validateProductRequests(products);

        List<ProductRequestDTO> creates = products.stream()
                .filter(product -> product.getId() == null || product.getId().isBlank())
                .toList();
        List<ProductRequestDTO> updates = products.stream()
                .filter(product -> product.getId() != null && !product.getId().isBlank())
                .toList();

        // Validate every row and resolve every update ID before any repository
        // method is called. This prevents DTO validation errors in a later
        // group or batch from allowing an earlier group to be saved.
        creates.forEach(productValidator::validate);
        Set<String> updateIds = updates.stream()
                .map(ProductRequestDTO::getId)
                .collect(Collectors.toSet());
        Map<String, ProductEntity> existingProducts = productRepository.findAllById(updateIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, product -> product));
        updates.forEach(product ->
                productValidator.validateForUpdate(product, existingProducts.keySet()));
        throwIfInvalid(products);

        if (importProperties.getBatchSize() < 1) {
            throw new IllegalArgumentException("Import batch size must be greater than zero");
        }

        List<ProductResponseDTO> imported = new java.util.ArrayList<>();
        for (int start = 0; start < creates.size(); start += importProperties.getBatchSize()) {
            int end = Math.min(start + importProperties.getBatchSize(), creates.size());
            imported.addAll(createProducts(creates.subList(start, end)));
        }
        for (int start = 0; start < updates.size(); start += importProperties.getBatchSize()) {
            int end = Math.min(start + importProperties.getBatchSize(), updates.size());
            imported.addAll(updateProducts(updates.subList(start, end)));
        }
        return imported;
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
    /**
     * Performs the delete product operation.
     */
    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
        log.info("Deleted product id={}", id);
    }

    @Override
    /**
     * Performs the delete all products operation.
     */
    public void deleteAllProducts() {
        productRepository.deleteAll();
        log.info("Deleted all products");
    }
}

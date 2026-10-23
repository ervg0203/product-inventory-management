package com.example.inventory.service.impl;

import com.example.inventory.dto.request.ProductRequestDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.dto.common.ImportResponseDTO;
import com.example.inventory.dto.common.PageResponseDTO;
import com.example.inventory.config.PaginationProperties;
import com.example.inventory.config.ImportProperties;
import com.example.inventory.entity.ProductEntity;
import com.example.inventory.exception.ProductNotFoundException;
import com.example.inventory.exception.ProductValidationException;
import com.example.inventory.mapper.ProductMapper;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.service.ProductService;
import com.example.inventory.util.ProductValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import com.example.inventory.dto.common.SearchRequestDTO;
import com.example.inventory.enums.ProductField;
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
    private final InventoryRepository inventoryRepository;
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
    public PageResponseDTO<ProductResponseDTO> searchProducts(SearchRequestDTO<ProductField> request) {
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

        Set<String> productSkus = productRequestDTOS.stream()
                .map(ProductRequestDTO::getSku)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, ProductEntity> existingProducts = productRepository.findAllBySkuIn(productSkus).stream()
                .collect(Collectors.toMap(ProductEntity::getSku, product -> product));

        Set<String> existingProductSkus = existingProducts.keySet();

        productRequestDTOS.forEach(product ->
                productValidator.validateForUpdate(product, existingProductSkus));
        throwIfInvalid(productRequestDTOS);

        productRequestDTOS.forEach(product ->
                productMapper.updateEntity(existingProducts.get(product.getSku()), product));

        List<ProductEntity> productEntities = productRequestDTOS.stream()
                .map(product -> existingProducts.get(product.getSku()))
                .toList();

        List<ProductResponseDTO> updatedProducts = productRepository.saveAll(productEntities).stream()
                .map(productMapper::toResponseDTO)
                .toList();

        log.info("Updated {} products", updatedProducts.size());
        return updatedProducts;
    }

    @Override
    public ImportResponseDTO<ProductResponseDTO> importProducts(List<ProductRequestDTO> products) {
        validateProductRequests(products);

        Set<String> skus = products.stream().map(ProductRequestDTO::getSku)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, ProductEntity> existingProducts = productRepository.findAllBySkuIn(skus).stream()
                .collect(Collectors.toMap(ProductEntity::getSku, product -> product));
        List<ProductRequestDTO> creates = products.stream()
                .filter(product -> !existingProducts.containsKey(product.getSku())).toList();
        List<ProductRequestDTO> updates = products.stream()
                .filter(product -> existingProducts.containsKey(product.getSku())).toList();

        creates.forEach(productValidator::validate);
        updates.forEach(product ->
                productValidator.validateForUpdate(product, existingProducts.keySet()));
        throwIfInvalid(products);

        if (importProperties.getBatchSize() < 1) {
            throw new IllegalArgumentException("Import batch size must be greater than zero");
        }

        List<ProductResponseDTO> createdProducts = new java.util.ArrayList<>();
        List<ProductResponseDTO> updatedProducts = new java.util.ArrayList<>();
        for (int start = 0; start < creates.size(); start += importProperties.getBatchSize()) {
            int end = Math.min(start + importProperties.getBatchSize(), creates.size());
            createdProducts.addAll(createProducts(creates.subList(start, end)));
        }
        for (int start = 0; start < updates.size(); start += importProperties.getBatchSize()) {
            int end = Math.min(start + importProperties.getBatchSize(), updates.size());
            updatedProducts.addAll(updateProducts(updates.subList(start, end)));
        }
        return ImportResponseDTO.<ProductResponseDTO>builder()
                .created(createdProducts)
                .updated(updatedProducts)
                .build();
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
        inventoryRepository.deleteByProductId(id);
        productRepository.deleteById(id);
        log.info("Deleted product id={}", id);
    }

    @Override
    public void deleteProductsByFilters(SearchRequestDTO<ProductField> request) {
        validateDeleteRequest(request);
        List<String> ids = mongoTemplate.find(searchQueryBuilder.build(request), ProductEntity.class)
                .stream().map(ProductEntity::getId).toList();
        if (ids.isEmpty()) {
            return;
        }
        inventoryRepository.deleteByProductIdIn(ids);
        productRepository.deleteAllById(ids);
        log.info("Deleted {} products matching filters", ids.size());
    }

    @Override
    /**
     * Performs the delete all products operation.
     */
    public void deleteAllProducts() {
        inventoryRepository.deleteAll();
        productRepository.deleteAll();
        log.info("Deleted all products");
    }

    private void validateDeleteRequest(SearchRequestDTO<ProductField> request) {
        if (request == null || request.getFilters() == null || request.getFilters().isEmpty()) {
            throw new IllegalArgumentException("At least one filter is required");
        }
    }
}

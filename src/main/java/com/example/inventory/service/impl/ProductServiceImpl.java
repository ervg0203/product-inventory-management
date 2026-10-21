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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import com.example.inventory.dto.request.ProductSearchRequestDTO;
import com.example.inventory.util.SearchQueryBuilder;
import com.example.inventory.enums.SortDirection;

import java.math.BigDecimal;
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
    private final MongoTemplate mongoTemplate;
    private final SearchQueryBuilder searchQueryBuilder;

    @Override
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

    @Override
    public PageResponseDTO<ProductResponseDTO> searchProductsByKeyword(String query, int page, int size) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be blank");
        }
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be zero or greater and size must be between 1 and 100");
        }

        Query searchQuery = searchQueryBuilder.buildKeywordQuery(query.trim());
        long total = mongoTemplate.count(Query.of(searchQuery), ProductEntity.class);
        searchQuery.skip((long) page * size).limit(size);
        List<ProductResponseDTO> content = mongoTemplate.find(searchQuery, ProductEntity.class).stream()
                .map(productMapper::toResponseDTO)
                .toList();
        int totalPages = (int) Math.ceil((double) total / size);

        return PageResponseDTO.<ProductResponseDTO>builder()
                .content(content)
                .totalRecords(total)
                .page(page)
                .pageSize(size)
                .totalPages(totalPages)
                .first(page == 0)
                .last(totalPages == 0 || page >= totalPages - 1)
                .build();
    }

    @Override
    public PageResponseDTO<ProductResponseDTO> searchProducts(String query, BigDecimal minPrice,
                                                              BigDecimal maxPrice, BigDecimal minRating,
                                                              BigDecimal maxRating, String category, String brand,
                                                              int page, int size, String sortBy,
                                                              SortDirection sortDirection) {
        validateStandardSearch(page, size, minPrice, maxPrice, minRating, maxRating);
        Query searchQuery = searchQueryBuilder.buildStandardQuery(
                query, minPrice, maxPrice, minRating, maxRating, category, brand, sortBy, sortDirection);
        return executeSearch(searchQuery, page, size);
    }

    private void validateStandardSearch(int page, int size, BigDecimal minPrice, BigDecimal maxPrice,
                                        BigDecimal minRating, BigDecimal maxRating) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be zero or greater and size must be between 1 and 100");
        }
        if (minPrice != null && minPrice.signum() < 0 || maxPrice != null && maxPrice.signum() < 0) {
            throw new IllegalArgumentException("Price values must be non-negative");
        }
        if (minRating != null && (minRating.signum() < 0 || minRating.compareTo(BigDecimal.valueOf(5)) > 0)
                || maxRating != null && (maxRating.signum() < 0 || maxRating.compareTo(BigDecimal.valueOf(5)) > 0)) {
            throw new IllegalArgumentException("Rating values must be between 0 and 5");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice cannot exceed maxPrice");
        }
        if (minRating != null && maxRating != null && minRating.compareTo(maxRating) > 0) {
            throw new IllegalArgumentException("minRating cannot exceed maxRating");
        }
    }

    private PageResponseDTO<ProductResponseDTO> executeSearch(Query query, int page, int size) {
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

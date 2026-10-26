package com.example.inventory.service;

import com.example.inventory.config.ImportProperties;
import com.example.inventory.config.PaginationProperties;
import com.example.inventory.dto.request.ProductRequestDTO;
import com.example.inventory.dto.common.*;
import com.example.inventory.enums.ProductField;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.entity.ProductEntity;
import com.example.inventory.exception.ProductNotFoundException;
import com.example.inventory.exception.ProductValidationException;
import com.example.inventory.mapper.ProductMapper;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.service.impl.ProductServiceImpl;
import com.example.inventory.util.ProductValidator;
import com.example.inventory.util.SearchCriteriaBuilder;
import com.example.inventory.util.SearchQueryBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.mongodb.core.query.Query;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    @Mock ProductRepository productRepository;
    @Mock InventoryRepository inventoryRepository;
    @Mock ProductMapper productMapper;
    @Mock ProductValidator productValidator;
    @Mock MongoTemplate mongoTemplate;
    @Mock SearchQueryBuilder searchQueryBuilder;
    @Mock SearchCriteriaBuilder searchCriteriaBuilder;
    @Mock ImportProperties importProperties;

    private ProductServiceImpl service() {
        return new ProductServiceImpl(productRepository, inventoryRepository, productMapper,
                productValidator, new PaginationProperties(), mongoTemplate,
                searchQueryBuilder, searchCriteriaBuilder, importProperties);
    }

    @Test
    void supportsPaginationLookupAndCascadingDelete() {
        ProductServiceImpl service = service();
        ProductEntity entity = ProductEntity.builder().id("p").sku("S").build();
        ProductResponseDTO response = ProductResponseDTO.builder().id("p").sku("S").build();
        when(productRepository.findAll(PageRequest.of(0, 2)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 2), 1));
        when(productMapper.toResponseDTO(entity)).thenReturn(response);
        assertEquals(1, service.getAllProducts(0, 2).getTotalRecords());
        when(productRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> service.getProductById("missing"));
        when(productRepository.findById("p")).thenReturn(Optional.of(entity));
        assertSame(response, service.getProductById("p"));
        assertThrows(IllegalArgumentException.class, () -> service.getAllProducts(-1, 2));
        when(productRepository.existsById("p")).thenReturn(true);
        service.deleteProduct("p");
        verify(inventoryRepository).deleteByProductId("p");
        verify(productRepository).deleteById("p");
        assertThrows(ProductNotFoundException.class, () -> service.deleteProduct("missing"));
    }

    @Test
    void preventsWritesForInvalidProductRequests() {
        ProductServiceImpl service = service();
        ProductRequestDTO invalid = ProductRequestDTO.builder().sku("S").isValid(false).build();
        when(productMapper.toResponseDTO(invalid)).thenReturn(ProductResponseDTO.builder().build());
        assertThrows(ProductValidationException.class, () -> service.createProducts(List.of(invalid)));
        verify(productRepository, never()).saveAll(any());
        assertThrows(IllegalArgumentException.class, () -> service.createProducts(List.of()));
        assertThrows(IllegalArgumentException.class, () -> service.createProducts(Arrays.asList(invalid, null)));
    }

    @Test
    void searchesProductsWithBothRequestShapesAndValidatesPages() {
        ProductServiceImpl service = service();
        Query query = new Query();
        when(searchQueryBuilder.build(any(SearchRequestDTO.class))).thenReturn(query);
        when(mongoTemplate.count(any(Query.class), eq(ProductEntity.class))).thenReturn(3L);
        ProductEntity entity = ProductEntity.builder().id("p").build();
        when(mongoTemplate.find(any(Query.class), eq(ProductEntity.class))).thenReturn(List.of(entity));
        when(productMapper.toResponseDTO(entity)).thenReturn(ProductResponseDTO.builder().id("p").build());
        SearchRequestDTO<ProductField> request = SearchRequestDTO.<ProductField>builder().page(0).size(2).build();
        assertEquals(2, service.searchProducts(request).getTotalPages());
        assertTrue(service.searchProducts(request).isFirst());
        request.setPage(1);
        assertTrue(service.searchProducts(request).isLast());
        assertThrows(IllegalArgumentException.class, () -> service.searchProducts((SearchRequestDTO<ProductField>) null));
        request.setPage(-1);
        assertThrows(IllegalArgumentException.class, () -> service.searchProducts(request));

        ProductSearchQueryDTO queryRequest = new ProductSearchQueryDTO();
        queryRequest.setPage(0); queryRequest.setSize(2);
        when(searchCriteriaBuilder.build(queryRequest)).thenReturn(SearchRequestDTO.<ProductField>builder().build());
        when(searchQueryBuilder.build(any(SearchRequestDTO.class), isNull())).thenReturn(query);
        assertEquals(2, service.searchProducts(queryRequest).getTotalPages());
        queryRequest.setSize(0);
        assertThrows(IllegalArgumentException.class, () -> service.searchProducts(queryRequest));
        queryRequest.setSize(101);
        assertThrows(IllegalArgumentException.class, () -> service.searchProducts(queryRequest));
        SearchRequestDTO<ProductField> defaults = SearchRequestDTO.<ProductField>builder().build();
        when(mongoTemplate.count(any(Query.class), eq(ProductEntity.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(ProductEntity.class))).thenReturn(List.of());
        assertTrue(service.searchProducts(defaults).isLast());
    }

    @Test
    void createsAndUpdatesValidProducts() {
        ProductServiceImpl service = service();
        ProductRequestDTO request = ProductRequestDTO.builder().sku("S").isValid(true).build();
        ProductEntity entity = ProductEntity.builder().sku("S").build();
        ProductResponseDTO response = ProductResponseDTO.builder().sku("S").build();
        when(productMapper.toEntity(request)).thenReturn(entity);
        when(productRepository.saveAll(any())).thenReturn(List.of(entity));
        when(productMapper.toResponseDTO(entity)).thenReturn(response);
        assertSame(response, service.createProduct(request));

        when(productRepository.findAllBySkuIn(Set.of("S"))).thenReturn(List.of(entity));
        when(productRepository.saveAll(any())).thenReturn(List.of(entity));
        when(productMapper.toResponseDTO(entity)).thenReturn(response);
        assertSame(response, service.updateProduct(request));
        verify(productMapper).updateEntity(entity, request);
    }

    @Test
    void updatesRejectUnknownSkuAndImportsBatches() {
        ProductServiceImpl service = service();
        ProductRequestDTO create = ProductRequestDTO.builder().sku("NEW").isValid(true).build();
        ProductRequestDTO update = ProductRequestDTO.builder().sku("OLD").isValid(true).build();
        ProductEntity existing = ProductEntity.builder().sku("OLD").build();
        when(productRepository.findAllBySkuIn(Set.of("NEW", "OLD"))).thenReturn(List.of(existing));
        when(productRepository.findAllBySkuIn(Set.of("NEW"))).thenReturn(List.of());
        when(productRepository.findAllBySkuIn(Set.of("OLD"))).thenReturn(List.of(existing));
        when(importProperties.getBatchSize()).thenReturn(1);
        when(productMapper.toEntity(create)).thenReturn(ProductEntity.builder().sku("NEW").build());
        when(productRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(productMapper.toResponseDTO(any(ProductEntity.class))).thenReturn(ProductResponseDTO.builder().build());
        assertEquals(1, service.importProducts(List.of(create, update)).getCreated().size());
        assertEquals(1, service.importProducts(List.of(create, update)).getUpdated().size());
        when(productRepository.findAllBySkuIn(Set.of("X"))).thenReturn(List.of());
        ProductRequestDTO unknown = ProductRequestDTO.builder().sku("X").isValid(false).build();
        when(productMapper.toResponseDTO(unknown)).thenReturn(ProductResponseDTO.builder().build());
        assertThrows(ProductValidationException.class, () -> service.updateProducts(List.of(unknown)));
        when(importProperties.getBatchSize()).thenReturn(0);
        assertThrows(IllegalArgumentException.class, () -> service.importProducts(List.of(create)));
    }

    @Test
    void deletesByFiltersAndDeletesAll() {
        ProductServiceImpl service = service();
        assertThrows(IllegalArgumentException.class, () -> service.deleteProductsByFilters(null));
        assertThrows(IllegalArgumentException.class, () -> service.deleteProductsByFilters(
                SearchRequestDTO.<ProductField>builder().build()));
        assertThrows(IllegalArgumentException.class, () -> service.deleteProductsByFilters(
                SearchRequestDTO.<ProductField>builder().filters(List.of()).build()));
        SearchRequestDTO<ProductField> request = SearchRequestDTO.<ProductField>builder()
                .filters(List.of(FilterDTO.<ProductField>builder().field(ProductField.SKU).build())).build();
        Query query = new Query();
        when(searchQueryBuilder.build(request)).thenReturn(query);
        when(mongoTemplate.find(query, ProductEntity.class)).thenReturn(List.of());
        service.deleteProductsByFilters(request);
        ProductEntity entity = ProductEntity.builder().id("p").build();
        when(mongoTemplate.find(query, ProductEntity.class)).thenReturn(List.of(entity));
        service.deleteProductsByFilters(request);
        verify(inventoryRepository).deleteByProductIdIn(List.of("p"));
        verify(productRepository).deleteAllById(List.of("p"));
        service.deleteAllProducts();
        verify(inventoryRepository).deleteAll();
        verify(productRepository).deleteAll();
    }
}

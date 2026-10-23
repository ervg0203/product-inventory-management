package com.example.inventory.service;

import com.example.inventory.config.ImportProperties;
import com.example.inventory.config.PaginationProperties;
import com.example.inventory.dto.response.WarehouseResponseDTO;
import com.example.inventory.dto.request.WarehouseRequestDTO;
import com.example.inventory.dto.common.*;
import com.example.inventory.enums.WarehouseField;
import com.example.inventory.entity.WarehouseEntity;
import com.example.inventory.exception.WarehouseNotFoundException;
import com.example.inventory.mapper.WarehouseMapper;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.WarehouseRepository;
import com.example.inventory.service.impl.WarehouseServiceImpl;
import com.example.inventory.util.SearchCriteriaBuilder;
import com.example.inventory.util.SearchQueryBuilder;
import com.example.inventory.util.WarehouseValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.mongodb.core.query.Query;
import com.example.inventory.exception.WarehouseValidationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceImplTest {
    @Mock WarehouseRepository warehouseRepository;
    @Mock InventoryRepository inventoryRepository;
    @Mock WarehouseMapper warehouseMapper;
    @Mock WarehouseValidator warehouseValidator;
    @Mock MongoTemplate mongoTemplate;
    @Mock SearchQueryBuilder searchQueryBuilder;
    @Mock SearchCriteriaBuilder searchCriteriaBuilder;

    private WarehouseServiceImpl service() {
        return new WarehouseServiceImpl(warehouseRepository, inventoryRepository, warehouseMapper,
                warehouseValidator, new PaginationProperties(), new ImportProperties(),
                mongoTemplate, searchQueryBuilder, searchCriteriaBuilder);
    }

    @Test
    void supportsPaginationLookupAndCascadingDelete() {
        WarehouseServiceImpl service = service();
        WarehouseEntity entity = WarehouseEntity.builder().id("w").code("W").build();
        when(warehouseRepository.findAll(PageRequest.of(0, 2)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 2), 1));
        when(warehouseMapper.toResponseDTO(entity))
                .thenReturn(WarehouseResponseDTO.builder().id("w").build());
        assertEquals(1, service.getAllWarehouses(0, 2).getTotalRecords());
        when(warehouseRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(WarehouseNotFoundException.class, () -> service.getWarehouseById("missing"));
        when(warehouseRepository.findById("w")).thenReturn(Optional.of(entity));
        assertEquals("w", service.getWarehouseById("w").getId());
        when(warehouseRepository.existsById("w")).thenReturn(true);
        service.deleteWarehouse("w");
        verify(inventoryRepository).deleteByWarehouseId("w");
        verify(warehouseRepository).deleteById("w");
        assertThrows(WarehouseNotFoundException.class, () -> service.deleteWarehouse("missing"));
    }

    @Test
    void searchesWarehousesWithBothRequestShapesAndValidatesPages() {
        WarehouseServiceImpl service = service();
        Query query = new Query();
        when(searchQueryBuilder.build(any(SearchRequestDTO.class))).thenReturn(query);
        when(mongoTemplate.count(any(Query.class), eq(WarehouseEntity.class))).thenReturn(3L);
        WarehouseEntity entity = WarehouseEntity.builder().id("w").build();
        when(mongoTemplate.find(any(Query.class), eq(WarehouseEntity.class))).thenReturn(List.of(entity));
        when(warehouseMapper.toResponseDTO(entity)).thenReturn(WarehouseResponseDTO.builder().id("w").build());
        SearchRequestDTO<WarehouseField> request = SearchRequestDTO.<WarehouseField>builder().page(0).size(2).build();
        assertEquals(2, service.searchWarehouses(request).getTotalPages());
        assertThrows(IllegalArgumentException.class, () -> service.searchWarehouses((SearchRequestDTO<WarehouseField>) null));
        request.setSize(101);
        assertThrows(IllegalArgumentException.class, () -> service.searchWarehouses(request));

        WarehouseSearchQueryDTO queryRequest = new WarehouseSearchQueryDTO();
        queryRequest.setPage(0); queryRequest.setSize(2); queryRequest.setQuery("London");
        when(searchCriteriaBuilder.build(queryRequest)).thenReturn(SearchRequestDTO.<WarehouseField>builder().build());
        when(searchQueryBuilder.build(any(SearchRequestDTO.class))).thenReturn(query);
        assertEquals(2, service.searchWarehouses(queryRequest).getTotalPages());
        queryRequest.setPage(-1);
        assertThrows(IllegalArgumentException.class, () -> service.searchWarehouses(queryRequest));
        WarehouseSearchQueryDTO defaults = new WarehouseSearchQueryDTO();
        when(searchCriteriaBuilder.build(defaults)).thenReturn(SearchRequestDTO.<WarehouseField>builder().build());
        when(mongoTemplate.count(any(Query.class), eq(WarehouseEntity.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(WarehouseEntity.class))).thenReturn(List.of());
        assertTrue(service.searchWarehouses(defaults).isLast());
    }

    @Test
    void createsAndUpdatesValidWarehouses() {
        WarehouseServiceImpl service = service();
        WarehouseRequestDTO request = WarehouseRequestDTO.builder().code("W").isValid(true).build();
        WarehouseEntity entity = WarehouseEntity.builder().code("W").build();
        WarehouseResponseDTO response = WarehouseResponseDTO.builder().id("w").build();
        when(warehouseMapper.toEntity(request)).thenReturn(entity);
        when(warehouseRepository.saveAll(any())).thenReturn(List.of(entity));
        when(warehouseMapper.toResponseDTO(entity)).thenReturn(response);
        assertSame(response, service.createWarehouse(request));

        when(warehouseRepository.findAllByCodeIn(Set.of("W"))).thenReturn(List.of(entity));
        assertSame(response, service.updateWarehouse(request));
        verify(warehouseMapper).updateEntity(entity, request);
    }

    @Test
    void rejectsInvalidListsAndImportsBatches() {
        WarehouseServiceImpl service = service();
        assertThrows(IllegalArgumentException.class, () -> service.createWarehouses(null));
        assertThrows(IllegalArgumentException.class, () -> service.createWarehouses(List.of()));
        assertThrows(IllegalArgumentException.class, () -> service.createWarehouses(java.util.Arrays.asList(null, WarehouseRequestDTO.builder().build())));
        WarehouseRequestDTO invalid = WarehouseRequestDTO.builder().code("W").isValid(false).build();
        when(warehouseMapper.toResponseDTO(invalid)).thenReturn(WarehouseResponseDTO.builder().build());
        assertThrows(WarehouseValidationException.class, () -> service.createWarehouses(List.of(invalid)));
        assertThrows(WarehouseValidationException.class, () -> service.updateWarehouses(List.of(invalid)));

        WarehouseRequestDTO create = WarehouseRequestDTO.builder().code("NEW").isValid(true).build();
        WarehouseRequestDTO update = WarehouseRequestDTO.builder().code("OLD").isValid(true).build();
        WarehouseEntity existing = WarehouseEntity.builder().code("OLD").build();
        when(warehouseRepository.findAllByCodeIn(Set.of("NEW", "OLD"))).thenReturn(List.of(existing));
        when(warehouseRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(warehouseMapper.toEntity(create)).thenReturn(WarehouseEntity.builder().code("NEW").build());
        when(warehouseMapper.toResponseDTO(any(WarehouseEntity.class))).thenReturn(WarehouseResponseDTO.builder().build());
        service.importWarehouses(List.of(create, update));
        assertEquals(1, service.importWarehouses(List.of(create)).getCreated().size());
        ImportProperties properties = new ImportProperties();
        properties.setBatchSize(0);
        WarehouseServiceImpl badBatch = new WarehouseServiceImpl(warehouseRepository, inventoryRepository,
                warehouseMapper, warehouseValidator, new PaginationProperties(), properties,
                mongoTemplate, searchQueryBuilder, searchCriteriaBuilder);
        assertThrows(IllegalArgumentException.class, () -> badBatch.importWarehouses(List.of(create)));
    }

    @Test
    void deletesByFiltersAndDeletesAll() {
        WarehouseServiceImpl service = service();
        assertThrows(IllegalArgumentException.class, () -> service.deleteWarehousesByFilters(null));
        assertThrows(IllegalArgumentException.class, () -> service.deleteWarehousesByFilters(
                SearchRequestDTO.<WarehouseField>builder().build()));
        assertThrows(IllegalArgumentException.class, () -> service.deleteWarehousesByFilters(
                SearchRequestDTO.<WarehouseField>builder().filters(List.of()).build()));
        SearchRequestDTO<WarehouseField> request = SearchRequestDTO.<WarehouseField>builder()
                .filters(List.of(FilterDTO.<WarehouseField>builder().field(WarehouseField.CODE).build())).build();
        Query query = new Query();
        when(searchQueryBuilder.build(request)).thenReturn(query);
        when(mongoTemplate.find(query, WarehouseEntity.class)).thenReturn(List.of());
        service.deleteWarehousesByFilters(request);
        WarehouseEntity entity = WarehouseEntity.builder().id("w").build();
        when(mongoTemplate.find(query, WarehouseEntity.class)).thenReturn(List.of(entity));
        service.deleteWarehousesByFilters(request);
        verify(inventoryRepository).deleteByWarehouseIdIn(List.of("w"));
        verify(warehouseRepository).deleteAllById(List.of("w"));
        service.deleteAllWarehouses();
        verify(inventoryRepository).deleteAll();
        verify(warehouseRepository).deleteAll();
    }
}

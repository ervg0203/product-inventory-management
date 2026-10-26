package com.example.inventory.service.impl;

import com.example.inventory.dto.request.WarehouseRequestDTO;
import com.example.inventory.dto.common.SearchRequestDTO;
import com.example.inventory.dto.common.WarehouseSearchQueryDTO;
import com.example.inventory.enums.WarehouseField;
import com.example.inventory.dto.common.PageResponseDTO;
import com.example.inventory.dto.common.ImportResponseDTO;
import com.example.inventory.config.PaginationProperties;
import com.example.inventory.config.ImportProperties;
import com.example.inventory.dto.response.WarehouseResponseDTO;
import com.example.inventory.entity.WarehouseEntity;
import com.example.inventory.exception.WarehouseNotFoundException;
import com.example.inventory.exception.WarehouseValidationException;
import com.example.inventory.mapper.WarehouseMapper;
import com.example.inventory.repository.WarehouseRepository;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.service.WarehouseService;
import com.example.inventory.util.WarehouseValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import com.example.inventory.util.SearchQueryBuilder;
import com.example.inventory.util.SearchCriteriaBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * Provides the warehouse service impl component.
 */
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;
    private final WarehouseMapper warehouseMapper;
    private final WarehouseValidator warehouseValidator;
    private final PaginationProperties paginationProperties;
    private final ImportProperties importProperties;
    private final MongoTemplate mongoTemplate;
    private final SearchQueryBuilder searchQueryBuilder;
    private final SearchCriteriaBuilder searchCriteriaBuilder;
/**
 * Executes the search warehouses operation.
 *
 * @param request input value
 * @return operation result
 */

    @Override
    public PageResponseDTO<WarehouseResponseDTO> searchWarehouses(SearchRequestDTO<WarehouseField> request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request cannot be null");
        }
        int page = request.getPage() == null ? paginationProperties.getDefaultPage() : request.getPage();
        int size = request.getSize() == null ? paginationProperties.getDefaultPageSize() : request.getSize();
        validatePage(page, size);

        Query query = searchQueryBuilder.build(request);
        long total = mongoTemplate.count(Query.of(query), WarehouseEntity.class);
        query.skip((long) page * size).limit(size);
        List<WarehouseResponseDTO> content = mongoTemplate.find(query, WarehouseEntity.class).stream()
                .map(warehouseMapper::toResponseDTO).toList();
        int totalPages = (int) Math.ceil((double) total / size);
        return PageResponseDTO.<WarehouseResponseDTO>builder().content(content).totalRecords(total)
                .page(page).pageSize(size).totalPages(totalPages)
                .first(page == 0).last(totalPages == 0 || page >= totalPages - 1).build();
    }
/**
 * Executes the search warehouses operation.
 *
 * @param request input value
 * @return operation result
 */

    @Override
    public PageResponseDTO<WarehouseResponseDTO> searchWarehouses(WarehouseSearchQueryDTO request) {
        SearchRequestDTO<WarehouseField> searchRequest = searchCriteriaBuilder.build(request);
        int page = request.getPage() == null ? paginationProperties.getDefaultPage() : request.getPage();
        int size = request.getSize() == null ? paginationProperties.getDefaultPageSize() : request.getSize();
        validatePage(page, size);
        Query query = searchQueryBuilder.build(searchRequest);
        searchCriteriaBuilder.addWarehouseKeywordCriteria(query, request.getQuery());
        long total = mongoTemplate.count(Query.of(query), WarehouseEntity.class);
        query.skip((long) page * size).limit(size);
        List<WarehouseResponseDTO> content = mongoTemplate.find(query, WarehouseEntity.class).stream()
                .map(warehouseMapper::toResponseDTO).toList();
        int totalPages = (int) Math.ceil((double) total / size);
        return PageResponseDTO.<WarehouseResponseDTO>builder().content(content).totalRecords(total)
                .page(page).pageSize(size).totalPages(totalPages)
                .first(page == 0).last(totalPages == 0 || page >= totalPages - 1).build();
    }


    @Override
    /**
     * Returns the result of the get all warehouses operation.
 * @param page input value
 * @param size input value
 * @return operation result
     */
    public PageResponseDTO<WarehouseResponseDTO> getAllWarehouses(int page, int size) {
        validatePage(page, size);
        Page<WarehouseEntity> warehousePage = warehouseRepository.findAll(PageRequest.of(page, size));
        log.info("Fetched warehouses page={}, size={}, totalRecords={}",
                page, size, warehousePage.getTotalElements());
        return PageResponseDTO.<WarehouseResponseDTO>builder()
                .content(warehousePage.getContent().stream().map(warehouseMapper::toResponseDTO).toList())
                .totalRecords(warehousePage.getTotalElements())
                .page(warehousePage.getNumber())
                .pageSize(warehousePage.getSize())
                .totalPages(warehousePage.getTotalPages())
                .build();
    }
/**
 * Executes the validate page operation.
 *
 * @param page input value
 * @param size input value
 */

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > paginationProperties.getMaxPageSize()) {
            throw new IllegalArgumentException("Page must be zero or greater and size must be between 1 and "
                    + paginationProperties.getMaxPageSize());
        }
    }

    @Override
    /**
     * Returns the result of the get warehouse by id operation.
 * @param id input value
 * @return operation result
     */
    public WarehouseResponseDTO getWarehouseById(String id) {
        return warehouseRepository.findById(id)
                .map(warehouseMapper::toResponseDTO)
                .orElseThrow(() -> new WarehouseNotFoundException(id));
    }

    @Override
    /**
     * Returns the result of the create warehouse operation.
 * @param request input value
 * @return operation result
     */
    public WarehouseResponseDTO createWarehouse(WarehouseRequestDTO request) {
        return createWarehouses(List.of(request)).getFirst();
    }

    @Override
    /**
     * Returns the result of the create warehouses operation.
 * @param requests input value
 * @return operation result
     */
    public List<WarehouseResponseDTO> createWarehouses(List<WarehouseRequestDTO> requests) {
        validateRequests(requests);
        requests.forEach(warehouseValidator::validate);
        throwIfInvalid(requests);
        return warehouseRepository.saveAll(requests.stream().map(warehouseMapper::toEntity).toList())
                .stream().map(warehouseMapper::toResponseDTO).toList();
    }

    @Override
    /**
     * Returns the result of the update warehouse operation.
 * @param request input value
 * @return operation result
     */
    public WarehouseResponseDTO updateWarehouse(WarehouseRequestDTO request) {
        return updateWarehouses(List.of(request)).getFirst();
    }

    @Override
    /**
     * Returns the result of the update warehouses operation.
 * @param requests input value
 * @return operation result
     */
    public List<WarehouseResponseDTO> updateWarehouses(List<WarehouseRequestDTO> requests) {
        validateRequests(requests);
        Set<String> codes = requests.stream().map(WarehouseRequestDTO::getCode)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, WarehouseEntity> existing = warehouseRepository.findAllByCodeIn(codes).stream()
                .collect(Collectors.toMap(WarehouseEntity::getCode, warehouse -> warehouse));
        requests.forEach(request -> warehouseValidator.validateForUpdate(request, existing.keySet()));
        throwIfInvalid(requests);
        requests.forEach(request -> warehouseMapper.updateEntity(existing.get(request.getCode()), request));
        return warehouseRepository.saveAll(requests.stream()
                        .map(request -> existing.get(request.getCode())).toList())
                .stream().map(warehouseMapper::toResponseDTO).toList();
    }
/**
 * Executes the import warehouses operation.
 *
 * @param warehouses input value
 * @return operation result
 */

    @Override
    public ImportResponseDTO<WarehouseResponseDTO> importWarehouses(List<WarehouseRequestDTO> warehouses) {
        validateRequests(warehouses);
        Set<String> codes = warehouses.stream().map(WarehouseRequestDTO::getCode)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, WarehouseEntity> existing = warehouseRepository.findAllByCodeIn(codes).stream()
                .collect(Collectors.toMap(WarehouseEntity::getCode, warehouse -> warehouse));
        List<WarehouseRequestDTO> creates = warehouses.stream()
                .filter(warehouse -> !existing.containsKey(warehouse.getCode())).toList();
        List<WarehouseRequestDTO> updates = warehouses.stream()
                .filter(warehouse -> existing.containsKey(warehouse.getCode())).toList();

        creates.forEach(warehouseValidator::validate);
        updates.forEach(warehouse ->
                warehouseValidator.validateForUpdate(warehouse, existing.keySet()));
        throwIfInvalid(warehouses);

        if (importProperties.getBatchSize() < 1) {
            throw new IllegalArgumentException("Import batch size must be greater than zero");
        }
        List<WarehouseResponseDTO> createdWarehouses = new ArrayList<>();
        List<WarehouseResponseDTO> updatedWarehouses = new ArrayList<>();
        for (int start = 0; start < creates.size(); start += importProperties.getBatchSize()) {
            int end = Math.min(start + importProperties.getBatchSize(), creates.size());
            createdWarehouses.addAll(createWarehouses(creates.subList(start, end)));
        }
        for (int start = 0; start < updates.size(); start += importProperties.getBatchSize()) {
            int end = Math.min(start + importProperties.getBatchSize(), updates.size());
            updatedWarehouses.addAll(updateWarehouses(updates.subList(start, end)));
        }
        return ImportResponseDTO.<WarehouseResponseDTO>builder()
                .created(createdWarehouses)
                .updated(updatedWarehouses)
                .build();
    }

    @Override
    /**
     * Performs the delete warehouse operation.
 * @param id input value
     */
    public void deleteWarehouse(String id) {
        if (!warehouseRepository.existsById(id)) {
            throw new WarehouseNotFoundException(id);
        }
        inventoryRepository.deleteByWarehouseId(id);
        warehouseRepository.deleteById(id);
        log.info("Deleted warehouse id={}", id);
    }
/**
 * Executes the delete warehouses by filters operation.
 *
 * @param request input value
 */

    @Override
    public void deleteWarehousesByFilters(SearchRequestDTO<WarehouseField> request) {
        if (request == null || request.getFilters() == null || request.getFilters().isEmpty()) {
            throw new IllegalArgumentException("At least one filter is required");
        }
        List<String> ids = mongoTemplate.find(searchQueryBuilder.build(request), WarehouseEntity.class)
                .stream().map(WarehouseEntity::getId).toList();
        if (ids.isEmpty()) {
            return;
        }
        inventoryRepository.deleteByWarehouseIdIn(ids);
        warehouseRepository.deleteAllById(ids);
        log.info("Deleted {} warehouses matching filters", ids.size());
    }

    @Override
    /**
     * Performs the delete all warehouses operation.
 * @param requests input value
     */
    public void deleteAllWarehouses() {
        inventoryRepository.deleteAll();
        warehouseRepository.deleteAll();
        log.info("Deleted all warehouses");
    }

    private void validateRequests(List<WarehouseRequestDTO> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("At least one warehouse is required");
        }
        if (requests.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Warehouse entries cannot be null");
        }
    }
/**
 * Executes the throw if invalid operation.
 *
 * @param requests input value
 */

    private void throwIfInvalid(List<WarehouseRequestDTO> requests) {
        List<WarehouseResponseDTO> invalid = requests.stream()
                .filter(request -> !Boolean.TRUE.equals(request.getIsValid()))
                .map(warehouseMapper::toResponseDTO).toList();
        if (!invalid.isEmpty()) {
            throw new WarehouseValidationException(invalid);
        }
    }
}

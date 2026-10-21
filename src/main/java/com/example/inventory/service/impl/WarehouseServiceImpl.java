package com.example.inventory.service.impl;

import com.example.inventory.dto.request.WarehouseRequestDTO;
import com.example.inventory.dto.common.PageResponseDTO;
import com.example.inventory.config.PaginationProperties;
import com.example.inventory.dto.response.WarehouseResponseDTO;
import com.example.inventory.entity.WarehouseEntity;
import com.example.inventory.exception.WarehouseNotFoundException;
import com.example.inventory.exception.WarehouseValidationException;
import com.example.inventory.mapper.WarehouseMapper;
import com.example.inventory.repository.WarehouseRepository;
import com.example.inventory.service.WarehouseService;
import com.example.inventory.util.WarehouseValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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
    private final WarehouseMapper warehouseMapper;
    private final WarehouseValidator warehouseValidator;
    private final PaginationProperties paginationProperties;

    @Override
    /**
     * Returns the result of the get all warehouses operation.
     */
    public PageResponseDTO<WarehouseResponseDTO> getAllWarehouses(int page, int size) {
        if (page < 0 || size < 1 || size > paginationProperties.getMaxPageSize()) {
            throw new IllegalArgumentException(
                    "Page must be zero or greater and size must be between 1 and "
                            + paginationProperties.getMaxPageSize());
        }
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

    @Override
    /**
     * Returns the result of the get warehouse by id operation.
     */
    public WarehouseResponseDTO getWarehouseById(String id) {
        return warehouseRepository.findById(id)
                .map(warehouseMapper::toResponseDTO)
                .orElseThrow(() -> new WarehouseNotFoundException(id));
    }

    @Override
    /**
     * Returns the result of the create warehouse operation.
     */
    public WarehouseResponseDTO createWarehouse(WarehouseRequestDTO request) {
        return createWarehouses(List.of(request)).getFirst();
    }

    @Override
    /**
     * Returns the result of the create warehouses operation.
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
     */
    public WarehouseResponseDTO updateWarehouse(WarehouseRequestDTO request) {
        return updateWarehouses(List.of(request)).getFirst();
    }

    @Override
    /**
     * Returns the result of the update warehouses operation.
     */
    public List<WarehouseResponseDTO> updateWarehouses(List<WarehouseRequestDTO> requests) {
        validateRequests(requests);
        Set<String> ids = requests.stream().map(WarehouseRequestDTO::getId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, WarehouseEntity> existing = warehouseRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(WarehouseEntity::getId, warehouse -> warehouse));
        requests.forEach(request -> warehouseValidator.validateForUpdate(request, existing.keySet()));
        throwIfInvalid(requests);
        requests.forEach(request -> warehouseMapper.updateEntity(existing.get(request.getId()), request));
        return warehouseRepository.saveAll(requests.stream()
                        .map(request -> existing.get(request.getId())).toList())
                .stream().map(warehouseMapper::toResponseDTO).toList();
    }

    @Override
    /**
     * Performs the delete warehouse operation.
     */
    public void deleteWarehouse(String id) {
        if (!warehouseRepository.existsById(id)) {
            throw new WarehouseNotFoundException(id);
        }
        warehouseRepository.deleteById(id);
        log.info("Deleted warehouse id={}", id);
    }

    @Override
    /**
     * Performs the delete all warehouses operation.
     */
    public void deleteAllWarehouses() {
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

    private void throwIfInvalid(List<WarehouseRequestDTO> requests) {
        List<WarehouseResponseDTO> invalid = requests.stream()
                .filter(request -> !Boolean.TRUE.equals(request.getIsValid()))
                .map(warehouseMapper::toResponseDTO).toList();
        if (!invalid.isEmpty()) {
            throw new WarehouseValidationException(invalid);
        }
    }
}

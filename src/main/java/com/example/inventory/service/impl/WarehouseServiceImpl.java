package com.example.inventory.service.impl;

import com.example.inventory.dto.request.WarehouseRequestDTO;
import com.example.inventory.dto.response.WarehousePageResponseDTO;
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
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;
    private final WarehouseValidator warehouseValidator;

    @Override
    public WarehousePageResponseDTO getAllWarehouses(int page, int size) {
        if (page < 0 || size < 1) {
            throw new IllegalArgumentException("Page must be zero or greater and size must be greater than zero");
        }
        Page<WarehouseEntity> warehousePage = warehouseRepository.findAll(PageRequest.of(page, size));
        log.info("Fetched warehouses page={}, size={}, totalRecords={}",
                page, size, warehousePage.getTotalElements());
        return WarehousePageResponseDTO.builder()
                .warehouses(warehousePage.getContent().stream().map(warehouseMapper::toResponseDTO).toList())
                .totalRecords(warehousePage.getTotalElements())
                .page(warehousePage.getNumber())
                .pageSize(warehousePage.getSize())
                .totalPages(warehousePage.getTotalPages())
                .build();
    }

    @Override
    public WarehouseResponseDTO getWarehouseById(String id) {
        return warehouseRepository.findById(id)
                .map(warehouseMapper::toResponseDTO)
                .orElseThrow(() -> new WarehouseNotFoundException(id));
    }

    @Override
    public WarehouseResponseDTO createWarehouse(WarehouseRequestDTO request) {
        return createWarehouses(List.of(request)).getFirst();
    }

    @Override
    public List<WarehouseResponseDTO> createWarehouses(List<WarehouseRequestDTO> requests) {
        validateRequests(requests);
        requests.forEach(warehouseValidator::validate);
        throwIfInvalid(requests);
        return warehouseRepository.saveAll(requests.stream().map(warehouseMapper::toEntity).toList())
                .stream().map(warehouseMapper::toResponseDTO).toList();
    }

    @Override
    public WarehouseResponseDTO updateWarehouse(WarehouseRequestDTO request) {
        return updateWarehouses(List.of(request)).getFirst();
    }

    @Override
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
    public void deleteWarehouse(String id) {
        if (!warehouseRepository.existsById(id)) {
            throw new WarehouseNotFoundException(id);
        }
        warehouseRepository.deleteById(id);
        log.info("Deleted warehouse id={}", id);
    }

    @Override
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

package com.example.inventory.service.impl;

import com.example.inventory.dto.request.InventoryRequestDTO;
import com.example.inventory.dto.request.SetStockRequestDTO;
import com.example.inventory.dto.request.StockAdjustmentRequestDTO;
import com.example.inventory.dto.response.InventoryResponseDTO;
import com.example.inventory.dto.response.ProductStockResponseDTO;
import com.example.inventory.entity.InventoryEntity;
import com.example.inventory.entity.WarehouseEntity;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.exception.InventoryConflictException;
import com.example.inventory.exception.InventoryNotFoundException;
import com.example.inventory.exception.InventoryValidationException;
import com.example.inventory.exception.ProductNotFoundException;
import com.example.inventory.exception.WarehouseNotFoundException;
import com.example.inventory.mapper.InventoryMapper;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.WarehouseRepository;
import com.example.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * Provides the inventory service impl component.
 */
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryMapper inventoryMapper;
    private final MongoTemplate mongoTemplate;

    @Override
    /**
     * Returns the result of the create inventory operation.
     */
    public InventoryResponseDTO createInventory(InventoryRequestDTO request) {
        requireProduct(request.getProductId());
        requireActiveWarehouse(request.getWarehouseId());
        if (inventoryRepository.findByProductIdAndWarehouseId(
                request.getProductId(), request.getWarehouseId()).isPresent()) {
            throw new InventoryConflictException("Inventory already exists for this product and warehouse");
        }

        InventoryEntity entity = inventoryMapper.toEntity(request);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        try {
            return inventoryMapper.toResponseDTO(inventoryRepository.save(entity));
        } catch (DuplicateKeyException exception) {
            throw new InventoryConflictException("Inventory already exists for this product and warehouse");
        }
    }

    @Override
    public List<InventoryResponseDTO> createInventories(List<InventoryRequestDTO> requests) {
        validateRequests(requests);
        requests.forEach(this::validateCreateRequest);
        throwIfInvalid(requests);

        LocalDateTime now = LocalDateTime.now();
        List<InventoryEntity> entities = requests.stream().map(request -> {
            InventoryEntity entity = inventoryMapper.toEntity(request);
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            return entity;
        }).toList();
        return inventoryRepository.saveAll(entities).stream()
                .map(inventoryMapper::toResponseDTO).toList();
    }

    @Override
    public List<InventoryResponseDTO> updateInventories(List<InventoryRequestDTO> requests) {
        validateRequests(requests);
        Set<String> ids = requests.stream().map(InventoryRequestDTO::getId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, InventoryEntity> existing = inventoryRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(InventoryEntity::getId, entity -> entity));

        requests.forEach(request -> validateUpdateRequest(request, existing));
        throwIfInvalid(requests);

        requests.forEach(request -> {
            InventoryEntity entity = existing.get(request.getId());
            entity.setProductId(request.getProductId());
            entity.setWarehouseId(request.getWarehouseId());
            entity.setQuantity(request.getQuantity());
            entity.setUpdatedAt(LocalDateTime.now());
        });
        return inventoryRepository.saveAll(requests.stream()
                        .map(request -> existing.get(request.getId())).toList())
                .stream().map(inventoryMapper::toResponseDTO).toList();
    }

    @Override
    /**
     * Returns the result of the get inventory by id operation.
     */
    public InventoryResponseDTO getInventoryById(String inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .map(inventoryMapper::toResponseDTO)
                .orElseThrow(() -> new InventoryNotFoundException(inventoryId));
    }

    @Override
    /**
     * Returns the result of the get product stock operation.
     */
    public ProductStockResponseDTO getProductStock(String productId) {
        requireProduct(productId);
        List<InventoryResponseDTO> inventory = inventoryRepository.findByProductId(productId)
                .stream()
                .map(inventoryMapper::toResponseDTO)
                .toList();
        return ProductStockResponseDTO.builder()
                .productId(productId)
                .inventory(inventory)
                .totalStock(inventory.stream().mapToInt(InventoryResponseDTO::getQuantity).sum())
                .build();
    }

    @Override
    /**
     * Returns the result of the get warehouse inventory operation.
     */
    public List<InventoryResponseDTO> getWarehouseInventory(String warehouseId) {
        requireWarehouse(warehouseId);
        return inventoryRepository.findByWarehouseId(warehouseId).stream()
                .map(inventoryMapper::toResponseDTO)
                .toList();
    }

    @Override
    /**
     * Returns the result of the set stock operation.
     */
    public InventoryResponseDTO setStock(String inventoryId, SetStockRequestDTO request) {
        InventoryEntity inventory = requireInventory(inventoryId);
        requireActiveWarehouse(inventory.getWarehouseId());
        InventoryEntity updated = mongoTemplate.findAndModify(
                Query.query(Criteria.where("_id").is(inventoryId)),
                /**
                 * Provides the update operation.
                 */
                new Update().set("quantity", request.getQuantity())
                        .set("updatedAt", LocalDateTime.now()),
                org.springframework.data.mongodb.core.FindAndModifyOptions.options().returnNew(true),
                InventoryEntity.class);
        return inventoryMapper.toResponseDTO(updated);
    }

    @Override
    /**
     * Returns the result of the increase stock operation.
     */
    public InventoryResponseDTO increaseStock(String inventoryId, StockAdjustmentRequestDTO request) {
        InventoryEntity inventory = requireInventory(inventoryId);
        requireActiveWarehouse(inventory.getWarehouseId());
        return inventoryMapper.toResponseDTO(updateStock(inventoryId, request.getQuantity(), false));
    }

    @Override
    /**
     * Returns the result of the decrease stock operation.
     */
    public InventoryResponseDTO decreaseStock(String inventoryId, StockAdjustmentRequestDTO request) {
        InventoryEntity inventory = requireInventory(inventoryId);
        requireActiveWarehouse(inventory.getWarehouseId());
        return inventoryMapper.toResponseDTO(updateStock(inventoryId, request.getQuantity(), true));
    }

    @Override
    /**
     * Returns the result of the get low stock operation.
     */
    public List<InventoryResponseDTO> getLowStock(int threshold, String warehouseId) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold must be non-negative");
        }
        if (warehouseId != null) {
            requireWarehouse(warehouseId);
        }
        List<InventoryEntity> records = warehouseId == null
                ? inventoryRepository.findByQuantityLessThanEqual(threshold)
                : inventoryRepository.findByWarehouseIdAndQuantityLessThanEqual(warehouseId, threshold);
        return records.stream().map(inventoryMapper::toResponseDTO).toList();
    }

    @Override
    /**
     * Performs the delete inventory operation.
     */
    public void deleteInventory(String inventoryId) {
        requireInventory(inventoryId);
        inventoryRepository.deleteById(inventoryId);
    }

    private InventoryEntity updateStock(String inventoryId, int amount, boolean decrease) {
        Criteria criteria = Criteria.where("_id").is(inventoryId);
        if (decrease) {
            criteria.and("quantity").gte(amount);
        }
        var result = mongoTemplate.updateFirst(
                Query.query(criteria),
                /**
                 * Provides the update operation.
                 */
                new Update().inc("quantity", decrease ? -amount : amount)
                        .set("updatedAt", LocalDateTime.now()),
                InventoryEntity.class);
        if (result.getMatchedCount() == 0) {
            if (decrease) {
                throw new InsufficientStockException("Insufficient stock for inventory: " + inventoryId);
            }
            throw new InventoryNotFoundException(inventoryId);
        }
        return requireInventory(inventoryId);
    }

    private InventoryEntity requireInventory(String inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException(inventoryId));
    }

    private void validateRequests(List<InventoryRequestDTO> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("At least one inventory record is required");
        }
        if (requests.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Inventory entries cannot be null");
        }
        requests.forEach(request -> {
            request.setErrors(new ArrayList<>());
            request.setIsValid(true);
        });
    }

    private void validateCreateRequest(InventoryRequestDTO request) {
        validateFields(request);
        if (isValid(request) && inventoryRepository.findByProductIdAndWarehouseId(
                request.getProductId(), request.getWarehouseId()).isPresent()) {
            addError(request, "productId",
                    "Inventory already exists for this product and warehouse");
        }
    }

    private void validateUpdateRequest(InventoryRequestDTO request,
                                       Map<String, InventoryEntity> existing) {
        validateFields(request);
        if (request.getId() == null || request.getId().isBlank()) {
            addError(request, "id", "Inventory ID is required for update");
        } else if (!existing.containsKey(request.getId())) {
            addError(request, "id", "Inventory ID does not exist");
        }
    }

    private void validateFields(InventoryRequestDTO request) {
        if (request.getProductId() == null || request.getProductId().isBlank()) {
            addError(request, "productId", "Product ID is required");
        } else if (!productRepository.existsById(request.getProductId())) {
            addError(request, "productId", "Product does not exist");
        }
        if (request.getWarehouseId() == null || request.getWarehouseId().isBlank()) {
            addError(request, "warehouseId", "Warehouse ID is required");
        } else {
            try {
                requireActiveWarehouse(request.getWarehouseId());
            } catch (RuntimeException exception) {
                addError(request, "warehouseId", exception.getMessage());
            }
        }
        if (request.getQuantity() == null || request.getQuantity() < 0) {
            addError(request, "quantity", "Quantity must be zero or greater");
        }
    }

    private boolean isValid(InventoryRequestDTO request) {
        return Boolean.TRUE.equals(request.getIsValid());
    }

    private void addError(InventoryRequestDTO request, String field, String message) {
        request.setIsValid(false);
        request.getErrors().add(com.example.inventory.util.Errors.builder()
                .field(field).message(message).build());
    }

    private void throwIfInvalid(List<InventoryRequestDTO> requests) {
        List<InventoryResponseDTO> invalid = requests.stream()
                .filter(request -> !Boolean.TRUE.equals(request.getIsValid()))
                .map(inventoryMapper::toResponseDTO).toList();
        if (!invalid.isEmpty()) {
            throw new InventoryValidationException(invalid);
        }
    }

    private void requireProduct(String productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }
    }

    private void requireWarehouse(String warehouseId) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new WarehouseNotFoundException(warehouseId);
        }
    }

    private void requireActiveWarehouse(String warehouseId) {
        WarehouseEntity warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));
        if (!warehouse.isActive()) {
            throw new InventoryConflictException("Warehouse " + warehouseId + " is not active");
        }
    }
}

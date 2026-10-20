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

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryMapper inventoryMapper;
    private final MongoTemplate mongoTemplate;

    @Override
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
    public InventoryResponseDTO getInventoryById(String inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .map(inventoryMapper::toResponseDTO)
                .orElseThrow(() -> new InventoryNotFoundException(inventoryId));
    }

    @Override
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
    public List<InventoryResponseDTO> getWarehouseInventory(String warehouseId) {
        requireWarehouse(warehouseId);
        return inventoryRepository.findByWarehouseId(warehouseId).stream()
                .map(inventoryMapper::toResponseDTO)
                .toList();
    }

    @Override
    public InventoryResponseDTO setStock(String inventoryId, SetStockRequestDTO request) {
        InventoryEntity inventory = requireInventory(inventoryId);
        requireActiveWarehouse(inventory.getWarehouseId());
        InventoryEntity updated = mongoTemplate.findAndModify(
                Query.query(Criteria.where("_id").is(inventoryId)),
                new Update().set("quantity", request.getQuantity())
                        .set("updatedAt", LocalDateTime.now()),
                org.springframework.data.mongodb.core.FindAndModifyOptions.options().returnNew(true),
                InventoryEntity.class);
        return inventoryMapper.toResponseDTO(updated);
    }

    @Override
    public InventoryResponseDTO increaseStock(String inventoryId, StockAdjustmentRequestDTO request) {
        InventoryEntity inventory = requireInventory(inventoryId);
        requireActiveWarehouse(inventory.getWarehouseId());
        return inventoryMapper.toResponseDTO(updateStock(inventoryId, request.getQuantity(), false));
    }

    @Override
    public InventoryResponseDTO decreaseStock(String inventoryId, StockAdjustmentRequestDTO request) {
        InventoryEntity inventory = requireInventory(inventoryId);
        requireActiveWarehouse(inventory.getWarehouseId());
        return inventoryMapper.toResponseDTO(updateStock(inventoryId, request.getQuantity(), true));
    }

    @Override
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

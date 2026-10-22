package com.example.inventory.repository;

import com.example.inventory.entity.InventoryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.*;

public interface InventoryRepository extends MongoRepository<InventoryEntity, String> {
    Optional<InventoryEntity> findByProductIdAndWarehouseId(String productId, String warehouseId);
    List<InventoryEntity> findByProductId(String productId);
    void deleteByProductId(String productId);
    List<InventoryEntity> findByWarehouseId(String warehouseId);
    void deleteByWarehouseId(String warehouseId);
    List<InventoryEntity> findByQuantityLessThanEqual(Integer threshold);
    List<InventoryEntity> findByWarehouseIdAndQuantityLessThanEqual(String warehouseId, Integer threshold);
}

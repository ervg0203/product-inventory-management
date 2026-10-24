package com.example.inventory.repository;

import com.example.inventory.entity.InventoryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.*;

public interface InventoryRepository extends MongoRepository<InventoryEntity, String> {
/**
 * Executes the find by product id and warehouse id operation.
 *
 * @param productId input value
 * @param warehouseId input value
 * @return operation result
 */
    Optional<InventoryEntity> findByProductIdAndWarehouseId(String productId, String warehouseId);
/**
 * Executes the find by product id operation.
 *
 * @param productId input value
 * @return operation result
 */
    List<InventoryEntity> findByProductId(String productId);
/**
 * Executes the delete by product id operation.
 *
 * @param productId input value
 */
    void deleteByProductId(String productId);
/**
 * Executes the delete by product id in operation.
 *
 * @param productIds input value
 */
    void deleteByProductIdIn(Iterable<String> productIds);
/**
 * Executes the find by warehouse id operation.
 *
 * @param warehouseId input value
 * @return operation result
 */
    List<InventoryEntity> findByWarehouseId(String warehouseId);
/**
 * Executes the delete by warehouse id operation.
 *
 * @param warehouseId input value
 */
    void deleteByWarehouseId(String warehouseId);
/**
 * Executes the delete by warehouse id in operation.
 *
 * @param warehouseIds input value
 */
    void deleteByWarehouseIdIn(Iterable<String> warehouseIds);
/**
 * Executes the find by quantity less than equal operation.
 *
 * @param threshold input value
 * @return operation result
 */
    List<InventoryEntity> findByQuantityLessThanEqual(Integer threshold);
/**
 * Executes the find by warehouse id and quantity less than equal operation.
 *
 * @param warehouseId input value
 * @param threshold input value
 * @return operation result
 */
    List<InventoryEntity> findByWarehouseIdAndQuantityLessThanEqual(String warehouseId, Integer threshold);
}

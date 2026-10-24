package com.example.inventory.service;

import com.example.inventory.dto.request.InventoryRequestDTO;
import com.example.inventory.dto.request.SetStockRequestDTO;
import com.example.inventory.dto.request.StockAdjustmentRequestDTO;
import com.example.inventory.dto.response.InventoryResponseDTO;
import com.example.inventory.dto.response.ProductStockResponseDTO;

import java.util.List;

public interface InventoryService {

    InventoryResponseDTO createInventory(InventoryRequestDTO request);
/**
 * Executes the create inventories operation.
 *
 * @param requests input value
 * @return operation result
 */
    List<InventoryResponseDTO> createInventories(List<InventoryRequestDTO> requests);
/**
 * Executes the update inventories operation.
 *
 * @param requests input value
// * @param productId input value
 * @return operation result
// * @param inventoryId input value
 */
    List<InventoryResponseDTO> updateInventories(List<InventoryRequestDTO> requests);

    InventoryResponseDTO getInventoryById(String inventoryId);

    ProductStockResponseDTO getProductStock(String productId);
/**
 * Executes the get warehouse inventory operation.
 *
 * @param warehouseId input value
// * @param inventoryId input value
// * @param request input value
 * @return operation result
 */

    List<InventoryResponseDTO> getWarehouseInventory(String warehouseId);

    InventoryResponseDTO setStock(String inventoryId, SetStockRequestDTO request);

    InventoryResponseDTO increaseStock(String inventoryId, StockAdjustmentRequestDTO request);

    InventoryResponseDTO decreaseStock(String inventoryId, StockAdjustmentRequestDTO request);
/**
 * Executes the get low stock operation.
 *
 * @param threshold input value
 * @param warehouseId input value
 * @return operation result
 */

    List<InventoryResponseDTO> getLowStock(int threshold, String warehouseId);
/**
 * Executes the delete inventory operation.
 *
 * @param inventoryId input value
 */

    void deleteInventory(String inventoryId);
}

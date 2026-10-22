package com.example.inventory.service;

import com.example.inventory.dto.request.InventoryRequestDTO;
import com.example.inventory.dto.request.SetStockRequestDTO;
import com.example.inventory.dto.request.StockAdjustmentRequestDTO;
import com.example.inventory.dto.response.InventoryResponseDTO;
import com.example.inventory.dto.response.ProductStockResponseDTO;

import java.util.List;

public interface InventoryService {

    InventoryResponseDTO createInventory(InventoryRequestDTO request);
    List<InventoryResponseDTO> createInventories(List<InventoryRequestDTO> requests);
    List<InventoryResponseDTO> updateInventories(List<InventoryRequestDTO> requests);

    InventoryResponseDTO getInventoryById(String inventoryId);

    ProductStockResponseDTO getProductStock(String productId);

    List<InventoryResponseDTO> getWarehouseInventory(String warehouseId);

    InventoryResponseDTO setStock(String inventoryId, SetStockRequestDTO request);

    InventoryResponseDTO increaseStock(String inventoryId, StockAdjustmentRequestDTO request);

    InventoryResponseDTO decreaseStock(String inventoryId, StockAdjustmentRequestDTO request);

    List<InventoryResponseDTO> getLowStock(int threshold, String warehouseId);

    void deleteInventory(String inventoryId);
}

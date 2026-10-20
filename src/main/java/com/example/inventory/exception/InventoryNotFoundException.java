package com.example.inventory.exception;

public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(String inventoryId) {
        super("Inventory not found with id: " + inventoryId);
    }

    public InventoryNotFoundException(String productId, String warehouseId) {
        super("Inventory not found for product " + productId + " in warehouse " + warehouseId);
    }
}

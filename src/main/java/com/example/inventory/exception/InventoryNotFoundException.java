package com.example.inventory.exception;

public class InventoryNotFoundException extends RuntimeException {
/**
 * Executes the inventory not found exception operation.
 *
 * @param inventoryId input value
 * @return operation result
 */
    public InventoryNotFoundException(String inventoryId) {
        super("Inventory not found with id: " + inventoryId);
    }
/**
 * Executes the inventory not found exception operation.
 *
 * @param productId input value
 * @param warehouseId input value
 * @return operation result
 */

    public InventoryNotFoundException(String productId, String warehouseId) {
        super("Inventory not found for product " + productId + " in warehouse " + warehouseId);
    }
}

package com.example.inventory.exception;

public class InsufficientStockException extends RuntimeException {
/**
 * Executes the insufficient stock exception operation.
 *
 * @param message input value
 * @return operation result
 */
    public InsufficientStockException(String message) {
        super(message);
    }
/**
 * Executes the insufficient stock exception operation.
 *
 * @param productId input value
 * @param warehouseId input value
 * @return operation result
 */

    public InsufficientStockException(String productId, String warehouseId) {
        super("Insufficient stock for product " + productId + " in warehouse " + warehouseId);
    }
}

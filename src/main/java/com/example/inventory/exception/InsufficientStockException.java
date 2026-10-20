package com.example.inventory.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String productId, String warehouseId) {
        super("Insufficient stock for product " + productId + " in warehouse " + warehouseId);
    }
}

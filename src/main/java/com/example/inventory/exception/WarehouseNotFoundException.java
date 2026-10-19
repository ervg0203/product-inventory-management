package com.example.inventory.exception;

public class WarehouseNotFoundException extends RuntimeException {
    public WarehouseNotFoundException(String id) {
        super("Warehouse not found with id: " + id);
    }
}

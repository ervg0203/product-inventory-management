package com.example.inventory.exception;

public class WarehouseNotFoundException extends RuntimeException {
/**
 * Executes the warehouse not found exception operation.
 *
 * @param id input value
 * @return operation result
 */
    public WarehouseNotFoundException(String id) {
        super("Warehouse not found with id: " + id);
    }
}

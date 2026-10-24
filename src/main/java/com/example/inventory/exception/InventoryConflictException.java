package com.example.inventory.exception;

public class InventoryConflictException extends RuntimeException {
    /**
     * Creates an inventory conflict exception.
     *
     * @param message conflict description
     */
    public InventoryConflictException(String message) { super(message); }
}

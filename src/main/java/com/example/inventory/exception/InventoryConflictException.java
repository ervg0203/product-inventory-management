package com.example.inventory.exception;

public class InventoryConflictException extends RuntimeException {
    public InventoryConflictException(String message) { super(message); }
}

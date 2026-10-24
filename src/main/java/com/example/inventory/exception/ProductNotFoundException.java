package com.example.inventory.exception;

public class ProductNotFoundException extends RuntimeException {
/**
 * Executes the product not found exception operation.
 *
 * @param id input value
 * @return operation result
 */

    public ProductNotFoundException(String id) {
        super("Product not found with id: " + id);
    }
}

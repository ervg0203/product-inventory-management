package com.example.inventory.exception;

import com.example.inventory.dto.response.InventoryResponseDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class InventoryValidationException extends RuntimeException {

    private final List<InventoryResponseDTO> inventories;
/**
 * Executes the inventory validation exception operation.
 *
 * @param inventories input value
 * @return operation result
 */

    public InventoryValidationException(List<InventoryResponseDTO> inventories) {
        super("One or more inventory records failed validation");
        this.inventories = inventories;
    }
}

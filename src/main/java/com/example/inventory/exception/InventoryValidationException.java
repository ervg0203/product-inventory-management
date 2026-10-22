package com.example.inventory.exception;

import com.example.inventory.dto.response.InventoryResponseDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class InventoryValidationException extends RuntimeException {

    private final List<InventoryResponseDTO> inventories;

    public InventoryValidationException(List<InventoryResponseDTO> inventories) {
        super("One or more inventory records failed validation");
        this.inventories = inventories;
    }
}

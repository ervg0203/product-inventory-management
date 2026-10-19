package com.example.inventory.exception;

import com.example.inventory.dto.response.WarehouseResponseDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class WarehouseValidationException extends RuntimeException {
    private final List<WarehouseResponseDTO> warehouses;

    public WarehouseValidationException(List<WarehouseResponseDTO> warehouses) {
        super("One or more warehouses failed validation");
        this.warehouses = warehouses;
    }
}

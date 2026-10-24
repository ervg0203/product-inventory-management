package com.example.inventory.exception;

import com.example.inventory.dto.response.WarehouseResponseDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class WarehouseValidationException extends RuntimeException {
    private final List<WarehouseResponseDTO> warehouses;
    private final int totalErrors;
    private final int totalIncorrectDtos;
/**
 * Executes the warehouse validation exception operation.
 *
 * @param warehouses input value
 * @return operation result
 */

    public WarehouseValidationException(List<WarehouseResponseDTO> warehouses) {
        super("One or more warehouses failed validation");
        this.warehouses = warehouses;
        this.totalIncorrectDtos = warehouses.size();
        this.totalErrors = warehouses.stream()
                .mapToInt(warehouse -> warehouse.getErrors() == null ? 0 : warehouse.getErrors().size())
                .sum();
    }
}

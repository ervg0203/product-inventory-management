package com.example.inventory.mapper;

import com.example.inventory.dto.request.InventoryRequestDTO;
import com.example.inventory.dto.response.InventoryResponseDTO;
import com.example.inventory.entity.InventoryEntity;
import org.springframework.stereotype.Component;

@Component
/**
 * Provides the inventory mapper component.
 */
public class InventoryMapper {
    /**
     * Returns the result of the to entity operation.
     */
    public InventoryEntity toEntity(InventoryRequestDTO request) {
        return InventoryEntity.builder()
                .productId(request.getProductId())
                .warehouseId(request.getWarehouseId()).quantity(request.getQuantity())
                .build();
    }
    /**
     * Returns the result of the to response dto operation.
     */
    public InventoryResponseDTO toResponseDTO(InventoryEntity entity) {
        return InventoryResponseDTO.builder().id(entity.getId()).productId(entity.getProductId())
                .warehouseId(entity.getWarehouseId()).quantity(entity.getQuantity())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt()).build();
    }
}

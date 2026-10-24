package com.example.inventory.mapper;

import com.example.inventory.dto.request.WarehouseRequestDTO;
import com.example.inventory.dto.response.WarehouseResponseDTO;
import com.example.inventory.entity.WarehouseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Component
/**
 * Provides the warehouse mapper component.
 */
public class WarehouseMapper {

    /**
     * Returns the result of the to entity operation.
 * @param request input value
 * @return operation result
     */
    public WarehouseEntity toEntity(WarehouseRequestDTO request) {
        LocalDateTime now = LocalDateTime.now();
        return WarehouseEntity.builder()
                .code(request.getCode())
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .active(request.isActive())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Performs the update entity operation.
 * @param entity input value
 * @param request input value
     */
    public void updateEntity(WarehouseEntity entity, WarehouseRequestDTO request) {
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setAddress(request.getAddress());
        entity.setCity(request.getCity());
        entity.setState(request.getState());
        entity.setPostalCode(request.getPostalCode());
        entity.setActive(request.isActive());
        entity.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Returns the result of the to response dto operation.
 * @param entity input value
 * @return operation result
     */
    public WarehouseResponseDTO toResponseDTO(WarehouseEntity entity) {
        return WarehouseResponseDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .address(entity.getAddress())
                .city(entity.getCity())
                .state(entity.getState())
                .postalCode(entity.getPostalCode())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .errors(new ArrayList<>())
                .isValid(true)
                .build();
    }

    /**
     * Returns the result of the to response dto operation.
 * @param request input value
 * @return operation result
     */
    public WarehouseResponseDTO toResponseDTO(WarehouseRequestDTO request) {
        return WarehouseResponseDTO.builder()
                .id(request.getId())
                .code(request.getCode())
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .active(request.isActive())
                .errors(request.getErrors())
                .isValid(request.getIsValid())
                .build();
    }
}

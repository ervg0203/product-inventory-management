package com.example.inventory.service;

import com.example.inventory.dto.request.WarehouseRequestDTO;
import com.example.inventory.dto.response.WarehousePageResponseDTO;
import com.example.inventory.dto.response.WarehouseResponseDTO;

import java.util.List;

public interface WarehouseService {
    WarehousePageResponseDTO getAllWarehouses(int page, int size);
    WarehouseResponseDTO getWarehouseById(String id);
    WarehouseResponseDTO createWarehouse(WarehouseRequestDTO request);
    List<WarehouseResponseDTO> createWarehouses(List<WarehouseRequestDTO> requests);
    WarehouseResponseDTO updateWarehouse(WarehouseRequestDTO request);
    List<WarehouseResponseDTO> updateWarehouses(List<WarehouseRequestDTO> requests);
    void deleteWarehouse(String id);
    void deleteAllWarehouses();
}

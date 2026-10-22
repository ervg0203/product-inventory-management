package com.example.inventory.service;

import com.example.inventory.dto.request.WarehouseRequestDTO;
import com.example.inventory.dto.response.WarehouseResponseDTO;
import com.example.inventory.dto.common.PageResponseDTO;
import com.example.inventory.dto.common.SearchRequestDTO;
import com.example.inventory.enums.WarehouseField;

import java.util.List;

public interface WarehouseService {
    PageResponseDTO<WarehouseResponseDTO> getAllWarehouses(int page, int size);
    PageResponseDTO<WarehouseResponseDTO> searchWarehouses(SearchRequestDTO<WarehouseField> request);
    WarehouseResponseDTO getWarehouseById(String id);
    WarehouseResponseDTO createWarehouse(WarehouseRequestDTO request);
    List<WarehouseResponseDTO> createWarehouses(List<WarehouseRequestDTO> requests);
    WarehouseResponseDTO updateWarehouse(WarehouseRequestDTO request);
    List<WarehouseResponseDTO> updateWarehouses(List<WarehouseRequestDTO> requests);
    List<WarehouseResponseDTO> importWarehouses(List<WarehouseRequestDTO> warehouses);
    void deleteWarehouse(String id);
    void deleteWarehousesByFilters(SearchRequestDTO<WarehouseField> request);
    void deleteAllWarehouses();
}

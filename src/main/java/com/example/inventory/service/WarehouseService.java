package com.example.inventory.service;

import com.example.inventory.dto.request.WarehouseRequestDTO;
import com.example.inventory.dto.response.WarehouseResponseDTO;
import com.example.inventory.dto.common.ImportResponseDTO;
import com.example.inventory.dto.common.PageResponseDTO;
import com.example.inventory.dto.common.SearchRequestDTO;
import com.example.inventory.dto.common.WarehouseSearchQueryDTO;
import com.example.inventory.enums.WarehouseField;

import java.util.List;

public interface WarehouseService {
    PageResponseDTO<WarehouseResponseDTO> getAllWarehouses(int page, int size);
    PageResponseDTO<WarehouseResponseDTO> searchWarehouses(SearchRequestDTO<WarehouseField> request);
    PageResponseDTO<WarehouseResponseDTO> searchWarehouses(WarehouseSearchQueryDTO request);
    WarehouseResponseDTO getWarehouseById(String id);
    WarehouseResponseDTO createWarehouse(WarehouseRequestDTO request);
/**
 * Executes the create warehouses operation.
 *
 * @param requests input value
 * @return operation result
 */
    List<WarehouseResponseDTO> createWarehouses(List<WarehouseRequestDTO> requests);
    WarehouseResponseDTO updateWarehouse(WarehouseRequestDTO request);
/**
 * Executes the update warehouses operation.
 *
 * @param requests input value
// * @param warehouses input value
 * @return operation result
 */
    List<WarehouseResponseDTO> updateWarehouses(List<WarehouseRequestDTO> requests);
    ImportResponseDTO<WarehouseResponseDTO> importWarehouses(List<WarehouseRequestDTO> warehouses);
/**
 * Executes the delete warehouse operation.
 *
 * @param id input value
 */
    void deleteWarehouse(String id);
/**
 * Executes the delete warehouses by filters operation.
 *
 * @param request input value
 */
    void deleteWarehousesByFilters(SearchRequestDTO<WarehouseField> request);
/**
 * Executes the delete all warehouses operation.
 *
 */
    void deleteAllWarehouses();
}

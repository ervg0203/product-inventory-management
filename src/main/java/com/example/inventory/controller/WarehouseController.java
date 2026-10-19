package com.example.inventory.controller;

import com.example.inventory.dto.common.ApiResponseDTO;
import com.example.inventory.dto.request.WarehouseRequestDTO;
import com.example.inventory.dto.common.PageResponseDTO;
import com.example.inventory.dto.response.WarehouseResponseDTO;
import com.example.inventory.service.WarehouseService;
import com.example.inventory.config.PaginationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final PaginationProperties paginationProperties;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<WarehouseResponseDTO>>> getAllWarehouses(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouses fetched successfully",
                warehouseService.getAllWarehouses(
                        page == null ? paginationProperties.getDefaultPage() : page,
                        size == null ? paginationProperties.getDefaultPageSize() : size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<WarehouseResponseDTO>> getWarehouseById(@PathVariable String id) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouse fetched successfully",
                warehouseService.getWarehouseById(id)));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponseDTO<WarehouseResponseDTO>> createWarehouse(
            @RequestBody WarehouseRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Warehouse created successfully",
                        warehouseService.createWarehouse(request)));
    }

    @PostMapping("/create-multi")
    public ResponseEntity<ApiResponseDTO<List<WarehouseResponseDTO>>> createWarehouses(
            @RequestBody List<WarehouseRequestDTO> requests) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Warehouses created successfully",
                        warehouseService.createWarehouses(requests)));
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponseDTO<WarehouseResponseDTO>> updateWarehouse(
            @RequestBody WarehouseRequestDTO request) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouse updated successfully",
                warehouseService.updateWarehouse(request)));
    }

    @PutMapping("/update-multi")
    public ResponseEntity<ApiResponseDTO<List<WarehouseResponseDTO>>> updateWarehouses(
            @RequestBody List<WarehouseRequestDTO> requests) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouses updated successfully",
                warehouseService.updateWarehouses(requests)));
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<ApiResponseDTO<Void>> deleteAllWarehouses() {
        warehouseService.deleteAllWarehouses();
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "All warehouses deleted successfully", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteWarehouse(@PathVariable String id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouse deleted successfully", null));
    }
}

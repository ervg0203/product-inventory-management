package com.example.inventory.controller;

import com.example.inventory.dto.common.ApiResponseDTO;
import com.example.inventory.dto.request.WarehouseRequestDTO;
import com.example.inventory.dto.common.PageResponseDTO;
import com.example.inventory.dto.response.WarehouseResponseDTO;
import com.example.inventory.service.WarehouseService;
import com.example.inventory.config.PaginationProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
@Tag(name = "Warehouses", description = "Manage warehouses used to store and organize inventory.")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final PaginationProperties paginationProperties;

    @GetMapping
    @Operation(summary = "Browse warehouses",
            description = "Returns a paginated list of warehouses. Page numbering starts at zero and defaults are configured in application properties.")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<WarehouseResponseDTO>>> getAllWarehouses(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouses fetched successfully",
                warehouseService.getAllWarehouses(
                        page == null ? paginationProperties.getDefaultPage() : page,
                        size == null ? paginationProperties.getDefaultPageSize() : size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a warehouse",
            description = "Returns complete details for one warehouse using its unique identifier.")
    public ResponseEntity<ApiResponseDTO<WarehouseResponseDTO>> getWarehouseById(@PathVariable String id) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouse fetched successfully",
                warehouseService.getWarehouseById(id)));
    }

    @PostMapping("/create")
    @Operation(summary = "Create a warehouse",
            description = "Creates one warehouse after validating its code, name, address, and location details.")
    public ResponseEntity<ApiResponseDTO<WarehouseResponseDTO>> createWarehouse(
            @RequestBody WarehouseRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Warehouse created successfully",
                        warehouseService.createWarehouse(request)));
    }

    @PostMapping("/create-multi")
    @Operation(summary = "Create warehouses in bulk",
            description = "Creates multiple warehouses in one request and returns validation details when entries are invalid.")
    public ResponseEntity<ApiResponseDTO<List<WarehouseResponseDTO>>> createWarehouses(
            @RequestBody List<WarehouseRequestDTO> requests) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Warehouses created successfully",
                        warehouseService.createWarehouses(requests)));
    }

    @PutMapping("/update")
    @Operation(summary = "Update a warehouse",
            description = "Updates one existing warehouse. The warehouse ID must refer to an existing warehouse.")
    public ResponseEntity<ApiResponseDTO<WarehouseResponseDTO>> updateWarehouse(
            @RequestBody WarehouseRequestDTO request) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouse updated successfully",
                warehouseService.updateWarehouse(request)));
    }

    @PutMapping("/update-multi")
    @Operation(summary = "Update warehouses in bulk",
            description = "Updates multiple existing warehouses in one request. Each warehouse ID must already exist.")
    public ResponseEntity<ApiResponseDTO<List<WarehouseResponseDTO>>> updateWarehouses(
            @RequestBody List<WarehouseRequestDTO> requests) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouses updated successfully",
                warehouseService.updateWarehouses(requests)));
    }

    @DeleteMapping("/delete-all")
    @Operation(summary = "Delete all warehouses",
            description = "Permanently removes every warehouse. Use this operation with caution.")
    public ResponseEntity<ApiResponseDTO<Void>> deleteAllWarehouses() {
        warehouseService.deleteAllWarehouses();
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "All warehouses deleted successfully", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a warehouse",
            description = "Permanently removes one warehouse using its unique identifier.")
    public ResponseEntity<ApiResponseDTO<Void>> deleteWarehouse(@PathVariable String id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouse deleted successfully", null));
    }
}

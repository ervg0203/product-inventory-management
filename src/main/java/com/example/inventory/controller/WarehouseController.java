package com.example.inventory.controller;

import com.example.inventory.dto.common.ApiResponseDTO;
import com.example.inventory.dto.request.WarehouseRequestDTO;
import com.example.inventory.dto.common.SearchRequestDTO;
import com.example.inventory.enums.WarehouseField;
import com.example.inventory.dto.common.PageResponseDTO;
import com.example.inventory.dto.response.WarehouseResponseDTO;
import com.example.inventory.service.WarehouseService;
import com.example.inventory.config.PaginationProperties;
import com.example.inventory.util.WarehouseExcelParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
@Tag(name = "Warehouses", description = "Manage warehouses used to store and organize inventory.")
/**
 * Provides the warehouse controller component.
 */
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final PaginationProperties paginationProperties;
    private final WarehouseExcelParser warehouseExcelParser;

    @GetMapping
    @Operation(summary = "Browse warehouses",
            description = "Returns a paginated list of warehouses. Page numbering starts at zero and defaults are configured in application properties.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Warehouses fetched successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid page or page size."),
            @ApiResponse(responseCode = "503", description = "Warehouse data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    /**
     * Handles the get all warehouses endpoint.
     */
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<WarehouseResponseDTO>>> getAllWarehouses(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouses fetched successfully",
                warehouseService.getAllWarehouses(
                        page == null ? paginationProperties.getDefaultPage() : page,
                        size == null ? paginationProperties.getDefaultPageSize() : size)));
    }

    @PostMapping("/search")
    @Operation(summary = "Search warehouses",
            description = "Searches warehouses using allowlisted fields and operators. Filters use AND semantics; "
                    + "page is zero-based and size is limited to the configured maximum.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Warehouses searched successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid search criteria, pagination, sorting, or value type."),
            @ApiResponse(responseCode = "503", description = "Warehouse data is temporarily unavailable.")
    })
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<WarehouseResponseDTO>>> searchWarehouses(
            @RequestBody SearchRequestDTO<WarehouseField> request) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouses searched successfully",
                warehouseService.searchWarehouses(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a warehouse",
            description = "Returns complete details for one warehouse using its unique identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Warehouse fetched successfully."),
            @ApiResponse(responseCode = "404", description = "Warehouse not found for the supplied ID."),
            @ApiResponse(responseCode = "503", description = "Warehouse data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    /**
     * Handles the get warehouse by id endpoint.
     */
    public ResponseEntity<ApiResponseDTO<WarehouseResponseDTO>> getWarehouseById(@PathVariable String id) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouse fetched successfully",
                warehouseService.getWarehouseById(id)));
    }

    @PostMapping("/create")
    @Operation(summary = "Create a warehouse",
            description = "Creates one warehouse after validating its code, name, address, and location details.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Warehouse created successfully."),
            @ApiResponse(responseCode = "400", description = "Malformed request or missing request data."),
            @ApiResponse(responseCode = "409", description = "A warehouse with the same code already exists."),
            @ApiResponse(responseCode = "422", description = "Warehouse validation failed. The response contains field-level errors."),
            @ApiResponse(responseCode = "503", description = "Warehouse data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    /**
     * Handles the create warehouse endpoint.
     */
    public ResponseEntity<ApiResponseDTO<WarehouseResponseDTO>> createWarehouse(
            @RequestBody WarehouseRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Warehouse created successfully",
                        warehouseService.createWarehouse(request)));
    }

    @PostMapping("/create-multi")
    @Operation(summary = "Create warehouses in bulk",
            description = "Creates multiple warehouses in one request and returns validation details when entries are invalid.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Warehouses created successfully."),
            @ApiResponse(responseCode = "400", description = "The list is empty, contains null entries, or the request is malformed."),
            @ApiResponse(responseCode = "409", description = "A warehouse with a duplicate code already exists."),
            @ApiResponse(responseCode = "422", description = "One or more warehouses failed validation."),
            @ApiResponse(responseCode = "503", description = "Warehouse data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    /**
     * Handles the create warehouses endpoint.
     */
    public ResponseEntity<ApiResponseDTO<List<WarehouseResponseDTO>>> createWarehouses(
            @RequestBody List<WarehouseRequestDTO> requests) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Warehouses created successfully",
                        warehouseService.createWarehouses(requests)));
    }

    @PutMapping("/update")
    @Operation(summary = "Update a warehouse",
            description = "Updates one existing warehouse. The warehouse ID must refer to an existing warehouse.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Warehouse updated successfully."),
            @ApiResponse(responseCode = "400", description = "Malformed request or missing warehouse ID."),
            @ApiResponse(responseCode = "404", description = "Warehouse not found for the supplied ID."),
            @ApiResponse(responseCode = "409", description = "A warehouse with the same code already exists."),
            @ApiResponse(responseCode = "422", description = "Warehouse validation failed."),
            @ApiResponse(responseCode = "503", description = "Warehouse data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    /**
     * Handles the update warehouse endpoint.
     */
    public ResponseEntity<ApiResponseDTO<WarehouseResponseDTO>> updateWarehouse(
            @RequestBody WarehouseRequestDTO request) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouse updated successfully",
                warehouseService.updateWarehouse(request)));
    }

    @PutMapping("/update-multi")
    @Operation(summary = "Update warehouses in bulk",
            description = "Updates multiple existing warehouses in one request. Each warehouse ID must already exist.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Warehouses updated successfully."),
            @ApiResponse(responseCode = "400", description = "The list is empty, contains null entries, or the request is malformed."),
            @ApiResponse(responseCode = "404", description = "One or more warehouse IDs do not exist."),
            @ApiResponse(responseCode = "409", description = "A warehouse with a duplicate code already exists."),
            @ApiResponse(responseCode = "422", description = "One or more warehouses failed validation."),
            @ApiResponse(responseCode = "503", description = "Warehouse data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    /**
     * Handles the update warehouses endpoint.
     */
    public ResponseEntity<ApiResponseDTO<List<WarehouseResponseDTO>>> updateWarehouses(
            @RequestBody List<WarehouseRequestDTO> requests) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouses updated successfully",
                warehouseService.updateWarehouses(requests)));
    }

    @PostMapping(value = "/import", consumes = "multipart/form-data")
    @Operation(summary = "Import warehouses from Excel",
            description = "Rows with an ID are updated and rows without an ID are created. "
                    + "All rows are validated before any row is persisted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Warehouses imported successfully."),
            @ApiResponse(responseCode = "400", description = "The file is empty, malformed, or has unsupported columns."),
            @ApiResponse(responseCode = "422", description = "One or more warehouse rows failed validation."),
            @ApiResponse(responseCode = "503", description = "Warehouse data is temporarily unavailable.")
    })
    public ResponseEntity<ApiResponseDTO<List<WarehouseResponseDTO>>> importWarehouses(
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouses imported successfully",
                warehouseService.importWarehouses(warehouseExcelParser.parse(file))));
    }

    @DeleteMapping("/delete-all")
    @Operation(summary = "Delete all warehouses",
            description = "Permanently removes every warehouse. Use this operation with caution.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All warehouses deleted successfully."),
            @ApiResponse(responseCode = "503", description = "Warehouse data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    /**
     * Handles the delete all warehouses endpoint.
     */
    public ResponseEntity<ApiResponseDTO<Void>> deleteAllWarehouses() {
        warehouseService.deleteAllWarehouses();
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "All warehouses deleted successfully", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a warehouse",
            description = "Permanently removes one warehouse using its unique identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Warehouse deleted successfully."),
            @ApiResponse(responseCode = "404", description = "Warehouse not found for the supplied ID."),
            @ApiResponse(responseCode = "503", description = "Warehouse data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    /**
     * Handles the delete warehouse endpoint.
     */
    public ResponseEntity<ApiResponseDTO<Void>> deleteWarehouse(@PathVariable String id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouse deleted successfully", null));
    }
}

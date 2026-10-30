package com.example.inventory.controller;

import com.example.inventory.dto.common.ApiResponseDTO;
import com.example.inventory.dto.common.ImportResponseDTO;
import com.example.inventory.dto.request.WarehouseRequestDTO;
import com.example.inventory.dto.common.SearchRequestDTO;
import com.example.inventory.dto.common.WarehouseSearchQueryDTO;
import com.example.inventory.enums.WarehouseField;
import com.example.inventory.dto.common.PageResponseDTO;
import com.example.inventory.dto.response.WarehouseResponseDTO;
import com.example.inventory.service.WarehouseService;
import com.example.inventory.config.PaginationProperties;
import com.example.inventory.util.WarehouseExcelParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
 * @param page input value
 * @param size input value
 * @return operation result
     */
    public ResponseEntity<ApiResponseDTO<List<WarehouseResponseDTO>>> getAllWarehouses(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        PageResponseDTO<WarehouseResponseDTO> response = warehouseService.getAllWarehouses(
                page == null ? paginationProperties.getDefaultPage() : page,
                size == null ? paginationProperties.getDefaultPageSize() : size);
        return paginatedResponse("Warehouses fetched successfully", response);
    }

    @PostMapping("/advance-search")
    @Operation(summary = "Search warehouses",
            description = "Searches warehouses using allowlisted fields and operators. Filters use AND semantics; "
                    + "page is zero-based and size is limited to the configured maximum.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Warehouses searched successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid search criteria, pagination, sorting, or value type."),
            @ApiResponse(responseCode = "503", description = "Warehouse data is temporarily unavailable.")
    })
/**
 * Executes the search warehouses operation.
 *
 * @param request input value
 * @return operation result
 */
    public ResponseEntity<ApiResponseDTO<List<WarehouseResponseDTO>>> searchWarehouses(
            @RequestBody SearchRequestDTO<WarehouseField> request) {
        return paginatedResponse("Warehouses searched successfully", warehouseService.searchWarehouses(request));
    }

    @GetMapping("/search")
    @Operation(summary = "Search warehouses with query parameters",
            description = "Searches warehouses using AND semantics for supplied filters and OR semantics across "
                    + "code, name, address, city, state, and postal code for query. If no criteria are supplied, "
                    + "this returns a paginated warehouse listing.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Warehouses searched successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid search criteria, pagination, or sorting."),
            @ApiResponse(responseCode = "503", description = "Warehouse data is temporarily unavailable.")
    })
    /**
     * Searches warehouses using the supplied query parameters.
     *
     * @param query keyword for warehouse text fields
     * @param code exact warehouse code
     * @param name partial warehouse name
     * @param city exact city
     * @param state exact state
     * @param active active-status filter
     * @param page zero-based page number
     * @param size requested page size
     * @param sortBy field used for sorting
     * @param sortDirection sort direction
     * @return paginated matching warehouses
     */
    public ResponseEntity<ApiResponseDTO<List<WarehouseResponseDTO>>> searchWarehousesByQuery(
            @Parameter(description = "Keyword across code, name, address, city, state, and postal code.",
                    example = "London")
            @RequestParam(required = false) String query,
            @Parameter(description = "Exact warehouse code.", example = "WH-001")
            @RequestParam(required = false) String code,
            @Parameter(description = "Partial, case-insensitive warehouse name.", example = "Central")
            @RequestParam(required = false) String name,
            @Parameter(description = "Partial, case-insensitive address.", example = "Main Street")
            @RequestParam(required = false) String address,
            @Parameter(description = "Partial, case-insensitive city.", example = "London")
            @RequestParam(required = false) String city,
            @Parameter(description = "Partial, case-insensitive state.", example = "California")
            @RequestParam(required = false) String state,
            @Parameter(description = "Partial, case-insensitive postal code.", example = "10001")
            @RequestParam(required = false) String postalCode,
            @Parameter(description = "Exact active status.", example = "true")
            @RequestParam(required = false) Boolean active,
            @Parameter(description = "Comma-separated exact warehouse codes.", example = "WH-001,WH-002")
            @RequestParam(required = false) String codes,
            @Parameter(description = "Comma-separated exact warehouse names.", example = "Central,North")
            @RequestParam(required = false) String names,
            @Parameter(description = "Comma-separated exact cities.", example = "London,Paris")
            @RequestParam(required = false) String cities,
            @Parameter(description = "Comma-separated exact states.", example = "CA,NY")
            @RequestParam(required = false) String states,
            @Parameter(description = "Zero-based page number.", example = "0",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "0", minimum = "0"))
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size, from 1 through 100.", example = "10",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "20", minimum = "1", maximum = "100"))
            @RequestParam(required = false) Integer size,
            @Parameter(description = "WarehouseEntity field to sort by.", example = "name")
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction: ASC or DESC.", example = "ASC")
            @RequestParam(required = false) String sortDirection) {
        WarehouseSearchQueryDTO request = new WarehouseSearchQueryDTO();
        request.setQuery(query);
        request.setCode(code);
        request.setName(name);
        request.setAddress(address);
        request.setCity(city);
        request.setState(state);
        request.setPostalCode(postalCode);
        request.setActive(active);
        request.setCodes(codes);
        request.setNames(names);
        request.setCities(cities);
        request.setStates(states);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortDirection(sortDirection);
        return paginatedResponse("Warehouses searched successfully", warehouseService.searchWarehouses(request));
    }

    private ResponseEntity<ApiResponseDTO<List<WarehouseResponseDTO>>> paginatedResponse(
            String message, PageResponseDTO<WarehouseResponseDTO> page) {
        return ResponseEntity.ok()
                .header("Total-Records", String.valueOf(page.getTotalRecords()))
                .header("Page", String.valueOf(page.getPage()))
                .header("Page-Size", String.valueOf(page.getPageSize()))
                .header("Total-Pages", String.valueOf(page.getTotalPages()))
                .header("First", String.valueOf(page.isFirst()))
                .header("Last", String.valueOf(page.isLast()))
                .body(new ApiResponseDTO<>(true, message, page.getContent(), "warehouses"));
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
 * @param id input value
 * @return operation result
     */
    public ResponseEntity<ApiResponseDTO<WarehouseResponseDTO>> getWarehouseById(@PathVariable String id) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouse fetched successfully",
                warehouseService.getWarehouseById(id), "warehouse"));
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
 * @param request input value
 * @return operation result
     */
    public ResponseEntity<ApiResponseDTO<WarehouseResponseDTO>> createWarehouse(
            @RequestBody WarehouseRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Warehouse created successfully",
                        warehouseService.createWarehouse(request), "warehouse"));
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
 * @param requests input value
 * @return operation result
     */
    public ResponseEntity<ApiResponseDTO<List<WarehouseResponseDTO>>> createWarehouses(
            @RequestBody List<WarehouseRequestDTO> requests) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Warehouses created successfully",
                        warehouseService.createWarehouses(requests), "warehouses"));
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
 * @param request input value
 * @return operation result
     */
    public ResponseEntity<ApiResponseDTO<WarehouseResponseDTO>> updateWarehouse(
            @RequestBody WarehouseRequestDTO request) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouse updated successfully",
                warehouseService.updateWarehouse(request), "warehouse"));
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
 * @param requests input value
 * @return operation result
     */
    public ResponseEntity<ApiResponseDTO<List<WarehouseResponseDTO>>> updateWarehouses(
            @RequestBody List<WarehouseRequestDTO> requests) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouses updated successfully",
                warehouseService.updateWarehouses(requests), "warehouses"));
    }

    @PostMapping(value = "/import", consumes = "multipart/form-data")
    @Operation(summary = "Import warehouses from Excel",
            description = "Rows with an ID are updated and rows without an ID are created. "
                    + "All rows are validated before any row is persisted. The response separates saved warehouses into created and updated lists.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Warehouses imported successfully."),
            @ApiResponse(responseCode = "400", description = "The file is empty, malformed, or has unsupported columns."),
            @ApiResponse(responseCode = "422", description = "One or more warehouse rows failed validation."),
            @ApiResponse(responseCode = "503", description = "Warehouse data is temporarily unavailable.")
    })
/**
 * Executes the import warehouses operation.
 *
 * @param file input value
 * @return operation result
 */
    public ResponseEntity<ApiResponseDTO<ImportResponseDTO<WarehouseResponseDTO>>> importWarehouses(
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouses imported successfully",
                warehouseService.importWarehouses(warehouseExcelParser.parse(file)), "warehouses"));
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
 * @param request input value
 * @return operation result
     */
    public ResponseEntity<ApiResponseDTO<Void>> deleteAllWarehouses() {
        warehouseService.deleteAllWarehouses();
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "All warehouses deleted successfully", null));
    }

    @DeleteMapping("/delete-by-filters")
    @Operation(summary = "Delete warehouses by filters",
            description = "Deletes all warehouses matching the supplied search filters and their related inventory records.")
    public ResponseEntity<ApiResponseDTO<Void>> deleteWarehousesByFilters(
            @RequestBody SearchRequestDTO<WarehouseField> request) {
        warehouseService.deleteWarehousesByFilters(request);
        return ResponseEntity.ok(new ApiResponseDTO<>(true,
                "Warehouses matching filters deleted successfully", null));
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
 * @param id input value
 * @return operation result
     */
    public ResponseEntity<ApiResponseDTO<Void>> deleteWarehouse(@PathVariable String id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouse deleted successfully", null));
    }
}

package com.example.inventory.controller;

import com.example.inventory.dto.common.ApiResponseDTO;
import com.example.inventory.dto.request.InventoryRequestDTO;
import com.example.inventory.dto.request.SetStockRequestDTO;
import com.example.inventory.dto.request.StockAdjustmentRequestDTO;
import com.example.inventory.dto.response.InventoryResponseDTO;
import com.example.inventory.dto.response.ProductStockResponseDTO;
import com.example.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Manage stock held for products across warehouses.")
/**
 * Provides the inventory controller component.
 */
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @Operation(summary = "Create inventory", description = "Creates stock for a product in an active warehouse.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inventory created successfully."),
            @ApiResponse(responseCode = "400", description = "Product ID, warehouse ID, or quantity is missing or invalid."),
            @ApiResponse(responseCode = "404", description = "The referenced product or warehouse was not found."),
            @ApiResponse(responseCode = "409", description = "Inventory already exists for this product and warehouse, or the warehouse is inactive."),
            @ApiResponse(responseCode = "503", description = "Inventory data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    /**
     * Handles the create inventory endpoint.
     */
    public ResponseEntity<ApiResponseDTO<InventoryResponseDTO>> createInventory(
            @Valid @RequestBody InventoryRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Inventory created successfully",
                        inventoryService.createInventory(request)));
    }

    @GetMapping("/{inventoryId}")
    @Operation(summary = "Get inventory", description = "Returns one inventory record by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventory fetched successfully."),
            @ApiResponse(responseCode = "404", description = "Inventory was not found for the supplied ID."),
            @ApiResponse(responseCode = "503", description = "Inventory data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    /**
     * Handles the get inventory endpoint.
     */
    public ResponseEntity<ApiResponseDTO<InventoryResponseDTO>> getInventory(
            @PathVariable String inventoryId) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Inventory fetched successfully",
                inventoryService.getInventoryById(inventoryId)));
    }

    @GetMapping("/products/{productId}")
    @Operation(summary = "Get product stock",
            description = "Returns inventory across all warehouses for a product and calculates the total stock.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product stock fetched successfully."),
            @ApiResponse(responseCode = "404", description = "Product was not found for the supplied ID."),
            @ApiResponse(responseCode = "503", description = "Inventory data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    /**
     * Handles the get product stock endpoint.
     */
    public ResponseEntity<ApiResponseDTO<ProductStockResponseDTO>> getProductStock(
            @PathVariable String productId) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Product stock fetched successfully",
                inventoryService.getProductStock(productId)));
    }

    @GetMapping("/warehouses/{warehouseId}")
    @Operation(summary = "Get warehouse inventory",
            description = "Returns all inventory records stored in a specific warehouse.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Warehouse inventory fetched successfully."),
            @ApiResponse(responseCode = "404", description = "Warehouse was not found for the supplied ID."),
            @ApiResponse(responseCode = "503", description = "Inventory data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    /**
     * Handles the get warehouse inventory endpoint.
     */
    public ResponseEntity<ApiResponseDTO<List<InventoryResponseDTO>>> getWarehouseInventory(
            @PathVariable String warehouseId) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Warehouse inventory fetched successfully",
                inventoryService.getWarehouseInventory(warehouseId)));
    }

    @PutMapping("/{inventoryId}/stock")
    @Operation(summary = "Set exact stock",
            description = "Replaces the current quantity with the supplied stock quantity.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock updated successfully."),
            @ApiResponse(responseCode = "400", description = "Quantity is missing or negative."),
            @ApiResponse(responseCode = "404", description = "Inventory was not found for the supplied ID."),
            @ApiResponse(responseCode = "409", description = "The inventory warehouse is inactive."),
            @ApiResponse(responseCode = "503", description = "Inventory data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    /**
     * Handles the set stock endpoint.
     */
    public ResponseEntity<ApiResponseDTO<InventoryResponseDTO>> setStock(
            @PathVariable String inventoryId,
            @Valid @RequestBody SetStockRequestDTO request) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Stock updated successfully",
                inventoryService.setStock(inventoryId, request)));
    }

    @PostMapping("/{inventoryId}/stock/increase")
    @Operation(summary = "Increase stock",
            description = "Adds the supplied quantity to an inventory record. The warehouse must be active.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock increased successfully."),
            @ApiResponse(responseCode = "400", description = "Adjustment quantity is missing or not positive."),
            @ApiResponse(responseCode = "404", description = "Inventory was not found for the supplied ID."),
            @ApiResponse(responseCode = "409", description = "The inventory warehouse is inactive."),
            @ApiResponse(responseCode = "503", description = "Inventory data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    /**
     * Handles the increase stock endpoint.
     */
    public ResponseEntity<ApiResponseDTO<InventoryResponseDTO>> increaseStock(
            @PathVariable String inventoryId,
            @Valid @RequestBody StockAdjustmentRequestDTO request) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Stock increased successfully",
                inventoryService.increaseStock(inventoryId, request)));
    }

    @PostMapping("/{inventoryId}/stock/decrease")
    @Operation(summary = "Decrease stock",
            description = "Removes the supplied quantity atomically. The operation fails if it would produce negative stock.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock decreased successfully."),
            @ApiResponse(responseCode = "400", description = "Adjustment quantity is missing or not positive."),
            @ApiResponse(responseCode = "404", description = "Inventory was not found for the supplied ID."),
            @ApiResponse(responseCode = "409", description = "Insufficient stock or the inventory warehouse is inactive."),
            @ApiResponse(responseCode = "503", description = "Inventory data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    /**
     * Handles the decrease stock endpoint.
     */
    public ResponseEntity<ApiResponseDTO<InventoryResponseDTO>> decreaseStock(
            @PathVariable String inventoryId,
            @Valid @RequestBody StockAdjustmentRequestDTO request) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Stock decreased successfully",
                inventoryService.decreaseStock(inventoryId, request)));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Find low-stock inventory",
            description = "Returns records whose quantity is less than or equal to the supplied threshold. Optionally filter by warehouse.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Low-stock inventory fetched successfully."),
            @ApiResponse(responseCode = "400", description = "Threshold is missing, negative, or otherwise invalid."),
            @ApiResponse(responseCode = "404", description = "The optional warehouse filter was not found."),
            @ApiResponse(responseCode = "503", description = "Inventory data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    /**
     * Handles the get low stock endpoint.
     */
    public ResponseEntity<ApiResponseDTO<List<InventoryResponseDTO>>> getLowStock(
            @RequestParam int threshold,
            @RequestParam(required = false) String warehouseId) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Low-stock inventory fetched successfully",
                inventoryService.getLowStock(threshold, warehouseId)));
    }

    @DeleteMapping("/{inventoryId}")
    @Operation(summary = "Delete inventory",
            description = "Deletes one inventory record without deleting its product or warehouse.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Inventory deleted successfully. No response body is returned."),
            @ApiResponse(responseCode = "404", description = "Inventory was not found for the supplied ID."),
            @ApiResponse(responseCode = "503", description = "Inventory data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    /**
     * Handles the delete inventory endpoint.
     */
    public ResponseEntity<Void> deleteInventory(@PathVariable String inventoryId) {
        inventoryService.deleteInventory(inventoryId);
        return ResponseEntity.noContent().build();
    }
}

package com.example.inventory.exception;

import com.example.inventory.dto.common.ErrorsDTO;
import com.example.inventory.dto.response.*;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesValidationExceptionsWithCountsAndPayloads() {
        ProductResponseDTO product = ProductResponseDTO.builder()
                .errors(List.of(ErrorsDTO.builder().field("sku").message("bad").build())).build();
        var productResult = handler.handleProductValidation(new ProductValidationException(List.of(product)));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, productResult.getStatusCode());
        assertEquals(1, productResult.getBody().getData().getTotalErrors());
        WarehouseResponseDTO warehouse = WarehouseResponseDTO.builder().errors(List.of()).build();
        var warehouseResult = handler.handleWarehouseValidation(new WarehouseValidationException(List.of(warehouse)));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, warehouseResult.getStatusCode());
        var inventoryResult = handler.handleInventoryValidation(new InventoryValidationException(
                List.of(InventoryResponseDTO.builder().build())));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, inventoryResult.getStatusCode());
    }

    @Test
    void handlesClientNotFoundConflictAndDataErrors() {
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleIllegalArgument(new IllegalArgumentException("bad")).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, handler.handleProductNotFound(new ProductNotFoundException("p")).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, handler.handleWarehouseNotFound(new WarehouseNotFoundException("w")).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, handler.handleInventoryNotFound(new InventoryNotFoundException("i")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, handler.handleInventoryConflict(new InventoryConflictException("conflict")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, handler.handleInventoryConflict(new InsufficientStockException("short")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, handler.handleDuplicateKey(new DuplicateKeyException("duplicate")).getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                handler.handleDataAccess(new DataAccessResourceFailureException("down")).getStatusCode());
    }

    @Test
    void handlesMalformedValidationAndUnexpectedErrors() {
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleMalformedRequest(new RuntimeException("malformed")).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, handler.handleUnexpectedException(new RuntimeException("unexpected")).getStatusCode());
        assertEquals("Inventory not found for product p in warehouse w",
                new InventoryNotFoundException("p", "w").getMessage());
        assertEquals("Insufficient stock for product p in warehouse w",
                new InsufficientStockException("p", "w").getMessage());
    }
}

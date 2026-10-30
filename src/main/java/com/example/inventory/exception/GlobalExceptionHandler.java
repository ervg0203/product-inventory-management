package com.example.inventory.exception;

import com.example.inventory.dto.common.ApiResponseDTO;
import com.example.inventory.dto.common.ValidationErrorResponseDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.dto.response.WarehouseResponseDTO;
import com.example.inventory.dto.response.InventoryResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestControllerAdvice
@Slf4j
/**
 * Provides the global exception handler component.
 */
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductValidationException.class)
    /**
     * Provides the handle product validation operation.
 * @param exception input value
 * @return operation result
     */
    public ResponseEntity<ApiResponseDTO<ValidationErrorResponseDTO<ProductResponseDTO>>> handleProductValidation(
            ProductValidationException exception) {
        log.info("Returning validation errors for {} products", exception.getProducts().size());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiResponseDTO<>(false, exception.getMessage(),
                        ValidationErrorResponseDTO.<ProductResponseDTO>builder()
                                .totalErrors(exception.getTotalErrors())
                                .totalIncorrectDtos(exception.getTotalIncorrectDtos())
                                .incorrectDtos(exception.getProducts())
                                .build(), "products"));
    }

    @ExceptionHandler(WarehouseValidationException.class)
    /**
     * Provides the handle warehouse validation operation.
 * @param exception input value
 * @return operation result
     */
    public ResponseEntity<ApiResponseDTO<ValidationErrorResponseDTO<WarehouseResponseDTO>>> handleWarehouseValidation(
            WarehouseValidationException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiResponseDTO<>(false, exception.getMessage(),
                        ValidationErrorResponseDTO.<WarehouseResponseDTO>builder()
                                .totalErrors(exception.getTotalErrors())
                                .totalIncorrectDtos(exception.getTotalIncorrectDtos())
                                .incorrectDtos(exception.getWarehouses())
                                .build(), "warehouses"));
    }
/**
 * Executes the handle inventory validation operation.
 *
 * @param exception input value
 * @return operation result
 */

    @ExceptionHandler(InventoryValidationException.class)
    public ResponseEntity<ApiResponseDTO<List<InventoryResponseDTO>>> handleInventoryValidation(
            InventoryValidationException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiResponseDTO<>(false, exception.getMessage(), exception.getInventories(), "inventories"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    /**
     * Returns the result of the handle illegal argument operation.
 * @param exception input value
 * @return operation result
     */
    public ResponseEntity<ApiResponseDTO<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        log.info("Invalid request: {}", exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiResponseDTO<>(false, exception.getMessage(), null));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    /**
     * Returns the result of the handle product not found operation.
 * @param exception input value
 * @return operation result
     */
    public ResponseEntity<ApiResponseDTO<Void>> handleProductNotFound(ProductNotFoundException exception) {
        log.info("Product not found: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponseDTO<>(false, exception.getMessage(), null));
    }

    @ExceptionHandler(WarehouseNotFoundException.class)
    /**
     * Returns the result of the handle warehouse not found operation.
 * @param exception input value
 * @return operation result
     */
    public ResponseEntity<ApiResponseDTO<Void>> handleWarehouseNotFound(WarehouseNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponseDTO<>(false, exception.getMessage(), null));
    }

    @ExceptionHandler({InventoryNotFoundException.class})
    /**
     * Returns the result of the handle inventory not found operation.
 * @param exception input value
 * @return operation result
     */
    public ResponseEntity<ApiResponseDTO<Void>> handleInventoryNotFound(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponseDTO<>(false, exception.getMessage(), null));
    }

    @ExceptionHandler({InventoryConflictException.class, InsufficientStockException.class})
    /**
     * Returns the result of the handle inventory conflict operation.
 * @param exception input value
 * @return operation result
     */
    public ResponseEntity<ApiResponseDTO<Void>> handleInventoryConflict(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponseDTO<>(false, exception.getMessage(), null));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    /**
     * Returns the result of the handle duplicate key operation.
 * @param exception input value
 * @return operation result
     */
    public ResponseEntity<ApiResponseDTO<Void>> handleDuplicateKey(DuplicateKeyException exception) {
        log.warn("Duplicate product request detected");
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponseDTO<>(false, "A product with the same SKU already exists", null));
    }

    @ExceptionHandler(DataAccessException.class)
    /**
     * Returns the result of the handle data access operation.
 * @param exception input value
 * @return operation result
     */
    public ResponseEntity<ApiResponseDTO<Void>> handleDataAccess(DataAccessException exception) {
        log.error("Product data access failure", exception);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiResponseDTO<>(false, "Product data is temporarily unavailable", null));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    /**
     * Returns the result of the handle malformed request operation.
 * @param exception input value
 * @return operation result
     */
    public ResponseEntity<ApiResponseDTO<Void>> handleMalformedRequest(Exception exception) {
        log.info("Malformed request received: {}", exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ApiResponseDTO<>(false, "Request is malformed or contains invalid parameters", null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    /**
     * Provides the handle method argument not valid operation.
 * @param exception input value
 * @return operation result
     */
    public ResponseEntity<ApiResponseDTO<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception) {
        log.info("Bean validation failed with {} field errors", exception.getBindingResult().getErrorCount());
        return ResponseEntity.badRequest()
                .body(new ApiResponseDTO<>(false, "Request validation failed", null));
    }

    @ExceptionHandler(Exception.class)
    /**
     * Returns the result of the handle unexpected exception operation.
 * @param exception input value
 * @return operation result
     */
    public ResponseEntity<ApiResponseDTO<Void>> handleUnexpectedException(Exception exception) {
        log.error("Unexpected application error", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDTO<>(false, "An unexpected error occurred", null));
    }
}

package com.example.inventory.exception;

import com.example.inventory.dto.common.ApiResponseDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductValidationException.class)
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> handleProductValidation(
            ProductValidationException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiResponseDTO<>(
                        false,
                        exception.getMessage(),
                        exception.getProducts()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiResponseDTO<>(false, exception.getMessage(), null));
    }
}

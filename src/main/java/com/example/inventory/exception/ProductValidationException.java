package com.example.inventory.exception;

import com.example.inventory.dto.response.ProductResponseDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class ProductValidationException extends RuntimeException {

    private final List<ProductResponseDTO> products;

    public ProductValidationException(List<ProductResponseDTO> products) {
        super("One or more products failed validation");
        this.products = products;
    }
}

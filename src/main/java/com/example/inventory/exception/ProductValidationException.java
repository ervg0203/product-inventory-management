package com.example.inventory.exception;

import lombok.Getter;

import java.util.List;
import com.example.inventory.dto.response.ProductResponseDTO;

@Getter
public class ProductValidationException extends RuntimeException {

    private final List<ProductResponseDTO> products;
    private final int totalErrors;
    private final int totalIncorrectDtos;
/**
 * Executes the product validation exception operation.
 *
 * @param products input value
 * @return operation result
 */

    public ProductValidationException(List<ProductResponseDTO> products) {
        super("One or more products failed validation");
        this.products = products;
        this.totalIncorrectDtos = products.size();
        this.totalErrors = products.stream()
                .mapToInt(product -> product.getErrors() == null ? 0 : product.getErrors().size())
                .sum();
    }
}

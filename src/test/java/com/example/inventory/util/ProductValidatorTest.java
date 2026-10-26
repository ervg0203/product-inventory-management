package com.example.inventory.util;

import com.example.inventory.dto.common.ErrorsDTO;
import com.example.inventory.dto.request.ProductRequestDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProductValidatorTest {
    private final ProductValidator validator = new ProductValidator();

    private ProductRequestDTO valid() {
        return ProductRequestDTO.builder().sku("SKU-1").title("Phone").description("Desc")
                .category("Tech").price(new BigDecimal("10.00")).discountPercentage(new BigDecimal("5"))
                .rating(new BigDecimal("4.5")).brand("Brand").returnPolicy("30 days").build();
    }

    @Test
    void validatesValidProductAndResetsPreviousErrors() {
        ProductRequestDTO product = valid();
        product.setIsValid(false);
        product.setErrors(List.of(ErrorsDTO.builder().field("old").message("old").build()));
        validator.validate(product);
        assertTrue(product.getIsValid());
        assertTrue(product.getErrors().isEmpty());
    }

    @Test
    void aggregatesRequiredAndRangeErrors() {
        ProductRequestDTO product = ProductRequestDTO.builder().sku("").title("x".repeat(151))
                .description(null).category(null).brand(null).returnPolicy(null)
                .price(BigDecimal.ZERO).discountPercentage(new BigDecimal("101"))
                .rating(new BigDecimal("-1")).build();
        validator.validate(product);
        assertFalse(product.getIsValid());
        assertTrue(product.getErrors().size() >= 8);
        assertTrue(product.getErrors().stream().anyMatch(e -> e.getField().equals("sku")));
        assertTrue(product.getErrors().stream().anyMatch(e -> e.getField().equals("price")));
    }

    @Test
    void rejectsPrecisionAndUpdateUnknownSku() {
        ProductRequestDTO product = valid();
        product.setPrice(new BigDecimal("1.234"));
        validator.validate(product);
        assertFalse(product.getIsValid());
        validator.validateForUpdate(valid(), Set.of("OTHER"));
        assertFalse(product.getIsValid());
    }

    @Test
    void doesNotDuplicateRequiredSkuErrorDuringUpdateValidation() {
        ProductRequestDTO product = ProductRequestDTO.builder().sku("").build();

        validator.validateForUpdate(product, Set.of());

        assertEquals(1, product.getErrors().stream()
                .filter(error -> "sku".equals(error.getField())
                        && "SKU is required".equals(error.getMessage()))
                .count());
    }

    @Test
    void nullIsIgnored() {
        assertDoesNotThrow(() -> validator.validate(null));
    }
}

package com.example.inventory.util;

import com.example.inventory.enums.ErrorMessage;
import com.example.inventory.enums.ProductField;
import com.example.inventory.dto.request.ProductRequestDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Set;

@Component
/**
 * Provides the product validator component.
 */
public final class ProductValidator {

    /**
     * Performs the validate operation.
     */
    public void validate(ProductRequestDTO product) {
        if (product == null) {
            return;
        }

        product.setErrors(new ArrayList<>());
        product.setIsValid(true);

        validateText(product, ProductField.SKU, product.getSku(), 50);
        validateText(product, ProductField.TITLE, product.getTitle(), 150);
        validateText(product, ProductField.DESCRIPTION, product.getDescription(), 2000);
        validateText(product, ProductField.CATEGORY, product.getCategory(), 100);
        validateText(product, ProductField.BRAND, product.getBrand(), 100);
        validateText(product, ProductField.RETURN_POLICY, product.getReturnPolicy(), 150);

        validateDecimal(product, ProductField.PRICE, product.getPrice(),
                false, null, 10, ErrorMessage.MUST_BE_POSITIVE, null);
        validateDecimal(product, ProductField.DISCOUNT_PERCENTAGE, product.getDiscountPercentage(),
                true, new BigDecimal("100"), 3,
                ErrorMessage.NEGATIVE, ErrorMessage.TOO_HIGH);
        validateDecimal(product, ProductField.RATING, product.getRating(),
                true, new BigDecimal("5"), 1,
                ErrorMessage.NEGATIVE, ErrorMessage.TOO_HIGH);

    }

    /**
     * Performs the validate for update operation.
     */
    public void validateForUpdate(ProductRequestDTO product, Set<String> existingProductSkus) {
        validate(product);

        if (product.getSku() == null || product.getSku().isBlank()) {
            addError(product, ErrorMessage.REQUIRED, ProductField.SKU);
        } else if (existingProductSkus == null || !existingProductSkus.contains(product.getSku())) {
            addError(product, ErrorMessage.NOT_FOUND, ProductField.SKU);
        }
    }

    private static void validateText(ProductRequestDTO product,
                                     ProductField field, String value, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            addError(product, ErrorMessage.REQUIRED, field);
        } else if (value.length() > maxLength) {
            addError(product, ErrorMessage.TOO_LONG, field);
        }
    }

    private static void validateDecimal(ProductRequestDTO product,
                                        ProductField field, BigDecimal value,
                                        boolean inclusiveMinimum, BigDecimal maximum,
                                        int integerDigits,
                                        ErrorMessage minimumMessage,
                                        ErrorMessage maximumMessage) {
        if (value == null) {
            addError(product, ErrorMessage.REQUIRED, field);
            return;
        }
        if ((inclusiveMinimum && value.compareTo(BigDecimal.ZERO) < 0)
                || (!inclusiveMinimum && value.compareTo(BigDecimal.ZERO) <= 0)) {
            addError(product, minimumMessage, field);
        } else if (maximum != null && value.compareTo(maximum) > 0) {
            addError(product, maximumMessage, field, maximum);
        } else if (value.scale() > 2
                || value.precision() - Math.max(value.scale(), 0) > integerDigits) {
            addError(product, ErrorMessage.SCALE, field, 2);
        }
    }

    private static void addError(ProductRequestDTO product,
                                 ErrorMessage message, ProductField field,
                                 Object... arguments) {
        product.setIsValid(false);
        Object[] messageArguments = new Object[arguments.length + 1];
        messageArguments[0] = field.getLabel();
        System.arraycopy(arguments, 0, messageArguments, 1, arguments.length);
        product.getErrors().add(Errors.builder()
                .message(message.format(messageArguments))
                .field(field.getValue())
                .build());
    }
}

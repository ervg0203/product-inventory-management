package com.example.inventory.util;

import com.example.inventory.dto.request.WarehouseRequestDTO;
import com.example.inventory.enums.ErrorMessage;
import com.example.inventory.enums.WarehouseField;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Set;

@Component
/**
 * Provides the warehouse validator component.
 */
public class WarehouseValidator {

    /**
     * Performs the validate operation.
     */
    public void validate(WarehouseRequestDTO warehouse) {
        if (warehouse == null) {
            return;
        }
        warehouse.setErrors(new ArrayList<>());
        warehouse.setIsValid(true);
        validateText(warehouse, WarehouseField.CODE, warehouse.getCode(), 50);
        validateText(warehouse, WarehouseField.NAME, warehouse.getName(), 150);
        validateText(warehouse, WarehouseField.ADDRESS, warehouse.getAddress(), 250);
        validateText(warehouse, WarehouseField.CITY, warehouse.getCity(), 100);
        validateText(warehouse, WarehouseField.STATE, warehouse.getState(), 100);
        validateText(warehouse, WarehouseField.POSTAL_CODE, warehouse.getPostalCode(), 20);
    }

    /**
     * Performs the validate for update operation.
     */
    public void validateForUpdate(WarehouseRequestDTO warehouse, Set<String> existingCodes) {
        validate(warehouse);
        if (warehouse == null) {
            return;
        }
        if (warehouse.getCode() == null || warehouse.getCode().isBlank()) {
            addError(warehouse, ErrorMessage.REQUIRED, WarehouseField.CODE);
        } else if (existingCodes == null || !existingCodes.contains(warehouse.getCode())) {
            addError(warehouse, ErrorMessage.NOT_FOUND, WarehouseField.CODE);
        }
    }

    private void validateText(WarehouseRequestDTO warehouse, WarehouseField field,
                              String value, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            addError(warehouse, ErrorMessage.REQUIRED, field);
        } else if (value.length() > maxLength) {
            addError(warehouse, ErrorMessage.TOO_LONG, field);
        }
    }

    private void addError(WarehouseRequestDTO warehouse, ErrorMessage message,
                          WarehouseField field, Object... arguments) {
        warehouse.setIsValid(false);
        Object[] messageArguments = new Object[arguments.length + 1];
        messageArguments[0] = field.getLabel();
        System.arraycopy(arguments, 0, messageArguments, 1, arguments.length);
        warehouse.getErrors().add(Errors.builder()
                .message(message.format(messageArguments))
                .field(field.getValue())
                .build());
    }
}

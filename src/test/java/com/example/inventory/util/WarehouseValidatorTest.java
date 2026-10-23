package com.example.inventory.util;

import com.example.inventory.dto.request.WarehouseRequestDTO;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class WarehouseValidatorTest {
    private final WarehouseValidator validator = new WarehouseValidator();

    private WarehouseRequestDTO valid() {
        return WarehouseRequestDTO.builder().code("WH-1").name("Central").address("Main")
                .city("London").state("London").postalCode("E1").active(true).build();
    }

    @Test
    void validatesAndAggregatesRequiredFields() {
        WarehouseRequestDTO warehouse = valid();
        validator.validate(warehouse);
        assertTrue(warehouse.getIsValid());
        warehouse.setName("");
        warehouse.setPostalCode("x".repeat(21));
        validator.validate(warehouse);
        assertFalse(warehouse.getIsValid());
        assertEquals(2, warehouse.getErrors().size());
    }

    @Test
    void updateRequiresExistingCode() {
        WarehouseRequestDTO warehouse = valid();
        validator.validateForUpdate(warehouse, Set.of("OTHER"));
        assertFalse(warehouse.getIsValid());
        assertEquals("code", warehouse.getErrors().getLast().getField());
    }
}

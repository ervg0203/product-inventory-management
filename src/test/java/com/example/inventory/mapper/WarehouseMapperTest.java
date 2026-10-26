package com.example.inventory.mapper;

import com.example.inventory.dto.request.WarehouseRequestDTO;
import com.example.inventory.dto.common.ErrorsDTO;
import com.example.inventory.entity.WarehouseEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WarehouseMapperTest {
    private final WarehouseMapper mapper = new WarehouseMapper();

    @Test
    void mapsWarehouseFields() {
        WarehouseRequestDTO request = WarehouseRequestDTO.builder().id("w")
                .code("W").name("Central").active(true).build();
        assertEquals("W", mapper.toEntity(request).getCode());
        assertTrue(mapper.toEntity(request).isActive());
    }

    @Test
    void mapsEntityAndInvalidRequestResponses() {
        WarehouseRequestDTO request = WarehouseRequestDTO.builder().id("w").code("W")
                .name("Central").address("Main").city("London").state("LN")
                .postalCode("E1").active(false).isValid(false)
                .errors(List.of(ErrorsDTO.builder().field("code").message("bad").build()))
                .build();
        WarehouseEntity entity = mapper.toEntity(request);
        entity.setId("w");
        assertEquals("London", mapper.toResponseDTO(entity).getCity());
        assertFalse(mapper.toResponseDTO(request).isValid());
        assertEquals(1, mapper.toResponseDTO(request).getErrors().size());
    }
}

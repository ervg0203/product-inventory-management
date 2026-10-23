package com.example.inventory.mapper;

import com.example.inventory.dto.request.InventoryRequestDTO;
import com.example.inventory.dto.common.ErrorsDTO;
import com.example.inventory.dto.response.InventoryResponseDTO;
import com.example.inventory.entity.InventoryEntity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InventoryMapperTest {
    private final InventoryMapper mapper = new InventoryMapper();

    @Test
    void mapsInventoryRequestAndResponse() {
        InventoryRequestDTO request = InventoryRequestDTO.builder().id("i")
                .productId("p").warehouseId("w").quantity(4).build();
        InventoryEntity entity = mapper.toEntity(request);
        assertEquals(4, entity.getQuantity());
        assertEquals("p", mapper.toResponseDTO(entity).getProductId());
    }

    @Test
    void mapsInvalidRequestResponseAndEntityMetadata() {
        InventoryRequestDTO request = InventoryRequestDTO.builder().id("i").productId("p")
                .warehouseId("w").quantity(4).isValid(false)
                .errors(java.util.List.of(ErrorsDTO.builder().field("quantity").message("bad").build()))
                .build();
        InventoryResponseDTO response = mapper.toResponseDTO(request);
        assertEquals("i", response.getId());
        assertFalse(response.isValid());
        assertEquals(1, response.getErrors().size());
        InventoryEntity entity = InventoryEntity.builder().id("i").productId("p")
                .warehouseId("w").quantity(4).build();
        assertTrue(mapper.toResponseDTO(entity).isValid());
    }
}

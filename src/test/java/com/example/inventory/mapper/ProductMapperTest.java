package com.example.inventory.mapper;

import com.example.inventory.dto.request.ProductRequestDTO;
import com.example.inventory.dto.common.ErrorsDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.entity.ProductEntity;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {
    private final ProductMapper mapper = new ProductMapper();

    @Test
    void mapsProductAndUpdatesMutableFields() {
        ProductRequestDTO request = ProductRequestDTO.builder().id("p").sku("S")
                .title("T").price(new BigDecimal("1.00")).build();
        ProductEntity entity = mapper.toEntity(request);
        assertEquals("S", entity.getSku());
        mapper.updateEntity(entity, ProductRequestDTO.builder().sku("S")
                .title("Updated").build());
        assertEquals("Updated", entity.getTitle());
    }

    @Test
    void mapsEntityAndInvalidRequestResponses() {
        ProductRequestDTO request = ProductRequestDTO.builder().id("p").sku("S")
                .title("T").description("D").category("C").price(new BigDecimal("1.00"))
                .discountPercentage(new BigDecimal("2")).rating(new BigDecimal("4"))
                .brand("B").returnPolicy("R").isValid(false)
                .errors(java.util.List.of(ErrorsDTO.builder().field("sku").message("bad").build()))
                .build();
        ProductEntity entity = mapper.toEntity(request);
        entity.setId("p");
        ProductResponseDTO entityResponse = mapper.toResponseDTO(entity);
        assertEquals("p", entityResponse.getId());
        assertEquals("D", entityResponse.getDescription());
        assertTrue(entityResponse.isValid());
        ProductResponseDTO requestResponse = mapper.toResponseDTO(request);
        assertFalse(requestResponse.isValid());
        assertEquals(1, requestResponse.getErrors().size());
    }
}

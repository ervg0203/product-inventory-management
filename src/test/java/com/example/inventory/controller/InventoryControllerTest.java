package com.example.inventory.controller;

import com.example.inventory.exception.GlobalExceptionHandler;
import com.example.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {
    @Mock InventoryService inventoryService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new InventoryController(inventoryService))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void createsInventoryAndRejectsInvalidValidatedRequest() throws Exception {
        mvc.perform(post("/api/v1/inventory/create").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"p\",\"warehouseId\":\"w\",\"quantity\":2}"))
                .andExpect(status().isCreated());
        verify(inventoryService).createInventory(any());

        mvc.perform(post("/api/v1/inventory/create").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"\",\"warehouseId\":\"w\",\"quantity\":-1}"))
                .andExpect(status().isBadRequest());
        verify(inventoryService, never()).createInventory(argThat(request ->
                request.getProductId().isEmpty()));
    }

    @Test
    void returnsNoContentForDeleteAndDelegatesLowStock() throws Exception {
        mvc.perform(delete("/api/v1/inventory/i")).andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(inventoryService).deleteInventory("i");

        mvc.perform(get("/api/v1/inventory/low-stock").param("threshold", "3")
                        .param("warehouseId", "w")).andExpect(status().isOk());
        verify(inventoryService).getLowStock(3, "w");
    }

    @Test
    void coversRemainingInventoryEndpoints() throws Exception {
        mvc.perform(post("/api/v1/inventory/create-multi").contentType(MediaType.APPLICATION_JSON)
                        .content("[]")).andExpect(status().isCreated());
        mvc.perform(put("/api/v1/inventory/update-multi").contentType(MediaType.APPLICATION_JSON)
                        .content("[]")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/inventory/i")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/inventory/products/p")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/inventory/warehouses/w")).andExpect(status().isOk());
        mvc.perform(put("/api/v1/inventory/i/stock").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2}")).andExpect(status().isOk());
        mvc.perform(patch("/api/v1/inventory/i/stock/increase").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2}")).andExpect(status().isOk());
        mvc.perform(patch("/api/v1/inventory/i/stock/decrease").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":1}")).andExpect(status().isOk());
        verify(inventoryService).createInventories(anyList());
        verify(inventoryService).updateInventories(anyList());
        verify(inventoryService).getInventoryById("i");
        verify(inventoryService).getProductStock("p");
        verify(inventoryService).getWarehouseInventory("w");
        verify(inventoryService).setStock(eq("i"), any());
        verify(inventoryService).increaseStock(eq("i"), any());
        verify(inventoryService).decreaseStock(eq("i"), any());
    }
}

package com.example.inventory.controller;

import com.example.inventory.config.PaginationProperties;
import com.example.inventory.dto.common.PageResponseDTO;
import com.example.inventory.dto.common.SearchRequestDTO;
import com.example.inventory.dto.common.WarehouseSearchQueryDTO;
import com.example.inventory.dto.response.WarehouseResponseDTO;
import com.example.inventory.exception.GlobalExceptionHandler;
import com.example.inventory.service.WarehouseService;
import com.example.inventory.util.WarehouseExcelParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WarehouseControllerTest {
    @Mock WarehouseService warehouseService;
    @Mock WarehouseExcelParser warehouseParser;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new WarehouseController(warehouseService,
                        new PaginationProperties(), warehouseParser))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void searchesCreatesInBulkAndDeletesAllWarehouses() throws Exception {
        when(warehouseService.searchWarehouses(any(WarehouseSearchQueryDTO.class)))
                .thenReturn(PageResponseDTO.<WarehouseResponseDTO>builder().content(List.of()).build());
        mvc.perform(get("/api/v1/warehouses/search").param("query", "London")
                        .param("active", "true").param("codes", "W1,W2").param("size", "5"))
                .andExpect(status().isOk());
        verify(warehouseService).searchWarehouses(any(WarehouseSearchQueryDTO.class));

        mvc.perform(post("/api/v1/warehouses/create-multi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"code\":\"W1\",\"active\":true}]"))
                .andExpect(status().isCreated());
        verify(warehouseService).createWarehouses(anyList());

        mvc.perform(delete("/api/v1/warehouses/delete-all"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").doesNotExist());
        verify(warehouseService).deleteAllWarehouses();
    }

    @Test
    void coversRemainingWarehouseEndpoints() throws Exception {
        when(warehouseService.getAllWarehouses(anyInt(), anyInt()))
                .thenReturn(PageResponseDTO.<WarehouseResponseDTO>builder().content(List.of()).build());
        when(warehouseService.searchWarehouses(any(SearchRequestDTO.class)))
                .thenReturn(PageResponseDTO.<WarehouseResponseDTO>builder().content(List.of()).build());
        mvc.perform(get("/api/v1/warehouses").param("page", "1").param("size", "2"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/warehouses/advance-search").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filters\":[]}")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/warehouses/w1")).andExpect(status().isOk());
        mvc.perform(post("/api/v1/warehouses/create").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"W\",\"active\":true}")).andExpect(status().isCreated());
        mvc.perform(put("/api/v1/warehouses/update").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"W\",\"active\":true}")).andExpect(status().isOk());
        mvc.perform(put("/api/v1/warehouses/update-multi").contentType(MediaType.APPLICATION_JSON)
                        .content("[]")).andExpect(status().isOk());
        mvc.perform(multipart("/api/v1/warehouses/import")
                        .file(new MockMultipartFile("file", "warehouses.xlsx",
                                "application/octet-stream", new byte[]{1}))).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/warehouses/delete-by-filters").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filters\":[]}")).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/warehouses/w1")).andExpect(status().isOk());
        verify(warehouseService).getAllWarehouses(1, 2);
        verify(warehouseService).searchWarehouses(any(SearchRequestDTO.class));
        verify(warehouseService).getWarehouseById("w1");
        verify(warehouseService).createWarehouse(any());
        verify(warehouseService).updateWarehouse(any());
        verify(warehouseService).updateWarehouses(anyList());
        verify(warehouseService).deleteWarehousesByFilters(any());
        verify(warehouseService).deleteWarehouse("w1");
    }
}

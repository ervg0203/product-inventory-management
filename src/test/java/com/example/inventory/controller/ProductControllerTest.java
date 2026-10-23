package com.example.inventory.controller;

import com.example.inventory.config.PaginationProperties;
import com.example.inventory.dto.common.PageResponseDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.exception.GlobalExceptionHandler;
import com.example.inventory.service.ProductService;
import com.example.inventory.util.ProductExcelParser;
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
class ProductControllerTest {
    @Mock ProductService productService;
    @Mock ProductExcelParser productParser;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ProductController(productService,
                        new PaginationProperties(), productParser))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void listsCreatesSearchesAndImportsProducts() throws Exception {
        PageResponseDTO<ProductResponseDTO> page = PageResponseDTO.<ProductResponseDTO>builder()
                .content(List.of()).totalRecords(0).page(0).pageSize(20).totalPages(0)
                .first(true).last(true).build();
        when(productService.getAllProducts(0, 20)).thenReturn(page);
        mvc.perform(get("/api/v1/products")).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.page").value(0));
        verify(productService).getAllProducts(0, 20);

        mvc.perform(post("/api/v1/products/create").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"S\",\"title\":\"T\"}"))
                .andExpect(status().isCreated());
        verify(productService).createProduct(any());

        mvc.perform(get("/api/v1/products/search").param("query", "phone")
                        .param("brands", "A,B").param("minPrice", "10")
                        .param("sortBy", "price").param("sortDirection", "DESC"))
                .andExpect(status().isOk());
        verify(productService).searchProducts(any(com.example.inventory.dto.common.ProductSearchQueryDTO.class));

        when(productParser.parse(any())).thenReturn(List.of());
        mvc.perform(multipart("/api/v1/products/import")
                        .file(new MockMultipartFile("file", "products.xlsx",
                                "application/octet-stream", new byte[]{1})))
                .andExpect(status().isOk());
        verify(productService).importProducts(anyList());
    }

    @Test
    void mapsServiceValidationErrors() throws Exception {
        doThrow(new IllegalArgumentException("bad page")).when(productService)
                .getAllProducts(-1, 20);
        mvc.perform(get("/api/v1/products").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("bad page"));
    }

    @Test
    void coversRemainingProductEndpoints() throws Exception {
        mvc.perform(post("/api/v1/products/advance-search").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filters\":[]}")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/products/p1")).andExpect(status().isOk());
        mvc.perform(post("/api/v1/products/create-multi").contentType(MediaType.APPLICATION_JSON)
                        .content("[]")).andExpect(status().isCreated());
        mvc.perform(put("/api/v1/products/update").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"S\"}")).andExpect(status().isOk());
        mvc.perform(put("/api/v1/products/update-multi").contentType(MediaType.APPLICATION_JSON)
                        .content("[]")).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/products/delete-all")).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/products/delete-by-filters").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filters\":[]}")).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/products/p1")).andExpect(status().isOk());
        verify(productService).searchProducts(any(com.example.inventory.dto.common.SearchRequestDTO.class));
        verify(productService).getProductById("p1");
        verify(productService).createProducts(anyList());
        verify(productService).updateProduct(any());
        verify(productService).updateProducts(anyList());
        verify(productService).deleteAllProducts();
        verify(productService).deleteProductsByFilters(any());
        verify(productService).deleteProduct("p1");
    }
}

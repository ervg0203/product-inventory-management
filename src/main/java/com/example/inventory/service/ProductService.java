package com.example.inventory.service;
import com.example.inventory.dto.request.ProductRequestDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.dto.common.PageResponseDTO;
import com.example.inventory.dto.common.SearchRequestDTO;
import com.example.inventory.enums.ProductField;

import java.util.List;

public interface ProductService {

    PageResponseDTO<ProductResponseDTO> getAllProducts(int page, int size);

    PageResponseDTO<ProductResponseDTO> searchProducts(SearchRequestDTO<ProductField> request);

    ProductResponseDTO getProductById(String id);

    ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO);

    List<ProductResponseDTO> createProducts(List<ProductRequestDTO> productRequestDTOS);

    ProductResponseDTO updateProduct(ProductRequestDTO productRequestDTOS);

    List<ProductResponseDTO> updateProducts(List<ProductRequestDTO> productRequestDTOS);

    List<ProductResponseDTO> importProducts(List<ProductRequestDTO> products);

    void deleteProduct(String id);
    void deleteProductsByFilters(SearchRequestDTO<ProductField> request);

    void deleteAllProducts();
}
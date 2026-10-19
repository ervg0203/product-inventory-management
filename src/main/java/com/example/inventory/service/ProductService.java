package com.example.inventory.service;
import com.example.inventory.dto.request.ProductRequestDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.dto.response.ProductPageResponseDTO;

import java.util.List;

public interface ProductService {

    ProductPageResponseDTO getAllProducts(int page, int size);

    ProductResponseDTO getProductById(String id);

    ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO);

    List<ProductResponseDTO> createProducts(List<ProductRequestDTO> productRequestDTOS);

    List<ProductResponseDTO> updateProducts(List<ProductRequestDTO> productRequestDTOS);

    void deleteProduct(String id);

    void deleteAllProducts();
}
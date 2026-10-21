package com.example.inventory.service;
import com.example.inventory.dto.request.ProductRequestDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.dto.common.PageResponseDTO;
import com.example.inventory.dto.request.ProductSearchRequestDTO;
import com.example.inventory.enums.SortDirection;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    PageResponseDTO<ProductResponseDTO> getAllProducts(int page, int size);

    PageResponseDTO<ProductResponseDTO> searchProducts(ProductSearchRequestDTO request);

    PageResponseDTO<ProductResponseDTO> searchProductsByKeyword(String query, int page, int size);

    PageResponseDTO<ProductResponseDTO> searchProducts(String query, BigDecimal minPrice, BigDecimal maxPrice,
                                                       BigDecimal minRating, BigDecimal maxRating,
                                                       String category, String brand, int page, int size,
                                                       String sortBy, SortDirection sortDirection);

    ProductResponseDTO getProductById(String id);

    ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO);

    List<ProductResponseDTO> createProducts(List<ProductRequestDTO> productRequestDTOS);

    ProductResponseDTO updateProduct(ProductRequestDTO productRequestDTOS);

    List<ProductResponseDTO> updateProducts(List<ProductRequestDTO> productRequestDTOS);

    void deleteProduct(String id);

    void deleteAllProducts();
}
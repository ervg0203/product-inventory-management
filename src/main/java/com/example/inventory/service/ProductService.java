package com.example.inventory.service;
import com.example.inventory.dto.request.CreateProductRequestDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.dto.response.ProductPageResponseDTO;

import java.util.List;

public interface ProductService {

    ProductPageResponseDTO getAllProducts(int page, int size);

    ProductResponseDTO getProductById(String id);

    ProductResponseDTO createProduct(CreateProductRequestDTO createProductRequestDTO);

    List<ProductResponseDTO> createProducts(List<CreateProductRequestDTO> createProductRequestDTOs);

}
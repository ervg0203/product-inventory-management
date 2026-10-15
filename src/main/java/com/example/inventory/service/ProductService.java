package com.example.inventory.service;
import com.example.inventory.dto.request.CreateProductRequestDTO;
import com.example.inventory.dto.response.ProductResponseDTO;

import java.util.List;

public interface ProductService {

    ProductResponseDTO createProduct(CreateProductRequestDTO createProductRequestDTO);

    List<ProductResponseDTO> createProducts(List<CreateProductRequestDTO> createProductRequestDTOs);

}
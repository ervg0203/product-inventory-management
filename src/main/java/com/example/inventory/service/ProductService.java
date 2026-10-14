package com.example.inventory.service;
import com.example.inventory.dto.request.CreateProductRequestDTO;
import com.example.inventory.dto.request.UpdateProductRequestDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import java.util.List;

public interface ProductService {

    ProductResponseDTO createProduct(CreateProductRequestDTO requestDTO);

    ProductResponseDTO getProductById(String id);

    List<ProductResponseDTO> getAllProducts();

    ProductResponseDTO updateProduct(
            String id,
            UpdateProductRequestDTO requestDTO
    );

    void deleteProduct(String id);
}
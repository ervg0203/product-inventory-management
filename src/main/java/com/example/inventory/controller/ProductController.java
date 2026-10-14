package com.example.inventory.controller;

import com.example.inventory.dto.request.CreateProductRequestDTO;
import com.example.inventory.dto.response.ApiResponseDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> getAllProducts() {

        List<ProductResponseDTO> products = productService.getAllProducts();

        ApiResponseDTO<List<ProductResponseDTO>> response =
                new ApiResponseDTO<>(
                        true,
                        "Products fetched successfully",
                        products);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> createProduct(@RequestBody CreateProductRequestDTO productRequest) {

        ProductResponseDTO product = productService.createProduct(productRequest);

        ApiResponseDTO<ProductResponseDTO> response =
                new ApiResponseDTO<>(
                        true,
                        "Product created successfully",
                        product);

        return ResponseEntity.ok(response);
    }

}

package com.example.inventory.controller;

import com.example.inventory.dto.request.CreateProductRequestDTO;
import com.example.inventory.dto.common.ApiResponseDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.service.ProductService;
import org.springframework.http.HttpStatus;
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

    @PostMapping("create")
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> createProduct(
            @RequestBody CreateProductRequestDTO productRequestDTO
    ) {

        ProductResponseDTO product = productService.createProduct(productRequestDTO);

        ApiResponseDTO<ProductResponseDTO> response =
                new ApiResponseDTO<>(
                        true,
                        "Product created successfully",
                        product);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/create/multi")
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> createProducts(
            @RequestBody List<CreateProductRequestDTO> productRequestDTOs
    ) {

        List<ProductResponseDTO> products = productService.createProducts(productRequestDTOs);

        ApiResponseDTO<List<ProductResponseDTO>> response =
                new ApiResponseDTO<>(
                        true,
                        "Products created successfully",
                        products);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}

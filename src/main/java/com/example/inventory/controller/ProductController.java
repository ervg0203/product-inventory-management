package com.example.inventory.controller;

import com.example.inventory.dto.request.ProductRequestDTO;
import com.example.inventory.dto.common.ApiResponseDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.dto.response.ProductPageResponseDTO;
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

    @GetMapping
    public ResponseEntity<ApiResponseDTO<ProductPageResponseDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ProductPageResponseDTO products = productService.getAllProducts(page, size);

        ApiResponseDTO<ProductPageResponseDTO> response =
                new ApiResponseDTO<>(
                        true,
                        "Products fetched successfully",
                        products);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> getProductById(
            @PathVariable String id) {

        ProductResponseDTO product = productService.getProductById(id);

        ApiResponseDTO<ProductResponseDTO> response =
                new ApiResponseDTO<>(
                        true,
                        "Product fetched successfully",
                        product);

        return ResponseEntity.ok(response);

    }

    @PostMapping("create")
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> createProduct(
            @RequestBody ProductRequestDTO productRequestDTO
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
            @RequestBody List<ProductRequestDTO> productRequestDTOs
    ) {

        List<ProductResponseDTO> products = productService.createProducts(productRequestDTOs);

        ApiResponseDTO<List<ProductResponseDTO>> response =
                new ApiResponseDTO<>(
                        true,
                        "Products created successfully",
                        products);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping
    public ResponseEntity<ApiResponseDTO<Void>> deleteAllProduct() {

        productService.deleteAllProducts();

        ApiResponseDTO<Void> response =
                new ApiResponseDTO<>(
                        true,
                        "All products deleted successfully",
                        null);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteProduct(
            @PathVariable String id) {

        productService.deleteProduct(id);

        ApiResponseDTO<Void> response =
                new ApiResponseDTO<>(
                        true,
                        "Product deleted successfully",
                        null);

        return ResponseEntity.ok(response);
    }

}

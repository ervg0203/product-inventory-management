package com.example.inventory.controller;

import com.example.inventory.dto.request.ProductRequestDTO;
import com.example.inventory.dto.common.ApiResponseDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.dto.response.ProductPageResponseDTO;
import com.example.inventory.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<ProductPageResponseDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Products fetched successfully",
                productService.getAllProducts(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> getProductById(
            @PathVariable String id) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Product fetched successfully",
                productService.getProductById(id)));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> createProduct(
            @RequestBody ProductRequestDTO productRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Product created successfully",
                        productService.createProduct(productRequestDTO)));
    }

    @PostMapping("/create-multi")
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> createProducts(
            @RequestBody List<ProductRequestDTO> productRequestDTOs) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Products created successfully",
                        productService.createProducts(productRequestDTOs)));
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> updateProducts(
            @RequestBody ProductRequestDTO productRequestDTOs) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Product updated successfully",
                productService.updateProduct(productRequestDTOs)));
    }

    @PutMapping("/update-multi")
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> updateProducts(
            @RequestBody List<ProductRequestDTO> productRequestDTOs) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Products updated successfully",
                productService.updateProducts(productRequestDTOs)));
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<ApiResponseDTO<Void>> deleteAllProduct() {
        productService.deleteAllProducts();
        return ResponseEntity.ok(new ApiResponseDTO<>(true,
                "All products deleted successfully", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteProduct(
            @PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(new ApiResponseDTO<>(true,
                "Product deleted successfully", null));
    }

}

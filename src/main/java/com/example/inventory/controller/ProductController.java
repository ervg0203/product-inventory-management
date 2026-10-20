package com.example.inventory.controller;

import com.example.inventory.dto.request.ProductRequestDTO;
import com.example.inventory.dto.common.ApiResponseDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.dto.common.PageResponseDTO;
import com.example.inventory.service.ProductService;
import com.example.inventory.config.PaginationProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Manage the product catalog, including creation, updates, lookup, pagination, and deletion.")
public class ProductController {

    private final ProductService productService;
    private final PaginationProperties paginationProperties;

    @GetMapping
    @Operation(summary = "Browse products",
            description = "Returns a paginated list of products. Page numbering starts at zero and defaults are configured in application properties.")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<ProductResponseDTO>>> getAllProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Products fetched successfully",
                productService.getAllProducts(
                        page == null ? paginationProperties.getDefaultPage() : page,
                        size == null ? paginationProperties.getDefaultPageSize() : size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product",
            description = "Returns complete details for one product using its unique identifier.")
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> getProductById(
            @PathVariable String id) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Product fetched successfully",
                productService.getProductById(id)));
    }

    @PostMapping("/create")
    @Operation(summary = "Create a product",
            description = "Creates one product after validating all required fields and business rules.")
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> createProduct(
            @RequestBody ProductRequestDTO productRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Product created successfully",
                        productService.createProduct(productRequestDTO)));
    }

    @PostMapping("/create-multi")
    @Operation(summary = "Create products in bulk",
            description = "Creates multiple products in one request. If any product is invalid, validation details are returned for the affected products.")
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> createProducts(
            @RequestBody List<ProductRequestDTO> productRequestDTOs) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Products created successfully",
                        productService.createProducts(productRequestDTOs)));
    }

    @PutMapping("/update")
    @Operation(summary = "Update a product",
            description = "Updates one existing product. The product ID must refer to an existing product.")
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> updateProducts(
            @RequestBody ProductRequestDTO productRequestDTOs) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Product updated successfully",
                productService.updateProduct(productRequestDTOs)));
    }

    @PutMapping("/update-multi")
    @Operation(summary = "Update products in bulk",
            description = "Updates multiple existing products in one request. Each product ID must already exist.")
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> updateProducts(
            @RequestBody List<ProductRequestDTO> productRequestDTOs) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Products updated successfully",
                productService.updateProducts(productRequestDTOs)));
    }

    @DeleteMapping("/delete-all")
    @Operation(summary = "Delete all products",
            description = "Permanently removes every product from the catalog. Use this operation with caution.")
    public ResponseEntity<ApiResponseDTO<Void>> deleteAllProduct() {
        productService.deleteAllProducts();
        return ResponseEntity.ok(new ApiResponseDTO<>(true,
                "All products deleted successfully", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product",
            description = "Permanently removes one product from the catalog using its unique identifier.")
    public ResponseEntity<ApiResponseDTO<Void>> deleteProduct(
            @PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(new ApiResponseDTO<>(true,
                "Product deleted successfully", null));
    }

}

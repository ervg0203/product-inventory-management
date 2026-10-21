package com.example.inventory.controller;

import com.example.inventory.dto.request.ProductRequestDTO;
import com.example.inventory.dto.common.ApiResponseDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.dto.common.PageResponseDTO;
import com.example.inventory.service.ProductService;
import com.example.inventory.config.PaginationProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products fetched successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid page or page size."),
            @ApiResponse(responseCode = "503", description = "Product data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product fetched successfully."),
            @ApiResponse(responseCode = "404", description = "Product not found for the supplied ID."),
            @ApiResponse(responseCode = "503", description = "Product data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> getProductById(
            @PathVariable String id) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Product fetched successfully",
                productService.getProductById(id)));
    }

    @PostMapping("/create")
    @Operation(summary = "Create a product",
            description = "Creates one product after validating all required fields and business rules.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully."),
            @ApiResponse(responseCode = "400", description = "Malformed request or missing request data."),
            @ApiResponse(responseCode = "409", description = "A product with the same SKU already exists."),
            @ApiResponse(responseCode = "422", description = "Product validation failed. The response contains field-level errors."),
            @ApiResponse(responseCode = "503", description = "Product data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> createProduct(
            @RequestBody ProductRequestDTO productRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Product created successfully",
                        productService.createProduct(productRequestDTO)));
    }

    @PostMapping("/create-multi")
    @Operation(summary = "Create products in bulk",
            description = "Creates multiple products in one request. If any product is invalid, validation details are returned for the affected products.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Products created successfully."),
            @ApiResponse(responseCode = "400", description = "The list is empty, contains null entries, or the request is malformed."),
            @ApiResponse(responseCode = "409", description = "A product with a duplicate SKU already exists."),
            @ApiResponse(responseCode = "422", description = "One or more products failed validation."),
            @ApiResponse(responseCode = "503", description = "Product data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> createProducts(
            @RequestBody List<ProductRequestDTO> productRequestDTOs) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, "Products created successfully",
                        productService.createProducts(productRequestDTOs)));
    }

    @PutMapping("/update")
    @Operation(summary = "Update a product",
            description = "Updates one existing product. The product ID must refer to an existing product.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully."),
            @ApiResponse(responseCode = "400", description = "Malformed request or missing product ID."),
            @ApiResponse(responseCode = "404", description = "Product not found for the supplied ID."),
            @ApiResponse(responseCode = "409", description = "A product with the same SKU already exists."),
            @ApiResponse(responseCode = "422", description = "Product validation failed."),
            @ApiResponse(responseCode = "503", description = "Product data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    public ResponseEntity<ApiResponseDTO<ProductResponseDTO>> updateProducts(
            @RequestBody ProductRequestDTO productRequestDTOs) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Product updated successfully",
                productService.updateProduct(productRequestDTOs)));
    }

    @PutMapping("/update-multi")
    @Operation(summary = "Update products in bulk",
            description = "Updates multiple existing products in one request. Each product ID must already exist.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products updated successfully."),
            @ApiResponse(responseCode = "400", description = "The list is empty, contains null entries, or the request is malformed."),
            @ApiResponse(responseCode = "404", description = "One or more product IDs do not exist."),
            @ApiResponse(responseCode = "409", description = "A product with a duplicate SKU already exists."),
            @ApiResponse(responseCode = "422", description = "One or more products failed validation."),
            @ApiResponse(responseCode = "503", description = "Product data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> updateProducts(
            @RequestBody List<ProductRequestDTO> productRequestDTOs) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Products updated successfully",
                productService.updateProducts(productRequestDTOs)));
    }

    @DeleteMapping("/delete-all")
    @Operation(summary = "Delete all products",
            description = "Permanently removes every product from the catalog. Use this operation with caution.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All products deleted successfully."),
            @ApiResponse(responseCode = "503", description = "Product data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    public ResponseEntity<ApiResponseDTO<Void>> deleteAllProduct() {
        productService.deleteAllProducts();
        return ResponseEntity.ok(new ApiResponseDTO<>(true,
                "All products deleted successfully", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product",
            description = "Permanently removes one product from the catalog using its unique identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product deleted successfully."),
            @ApiResponse(responseCode = "404", description = "Product not found for the supplied ID."),
            @ApiResponse(responseCode = "503", description = "Product data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    public ResponseEntity<ApiResponseDTO<Void>> deleteProduct(
            @PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(new ApiResponseDTO<>(true,
                "Product deleted successfully", null));
    }

}

package com.example.inventory.controller;

import com.example.inventory.dto.request.ProductRequestDTO;
import com.example.inventory.dto.common.SearchRequestDTO;
import com.example.inventory.enums.ProductField;
import com.example.inventory.dto.common.ApiResponseDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.dto.common.PageResponseDTO;
import com.example.inventory.service.ProductService;
import com.example.inventory.config.PaginationProperties;
import com.example.inventory.util.ProductExcelParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Manage the product catalog, including creation, updates, lookup, pagination, and deletion.")
/**
 * Provides the product controller component.
 */
public class ProductController {

    private final ProductService productService;
    private final PaginationProperties paginationProperties;
    private final ProductExcelParser productExcelParser;

    @GetMapping
    @Operation(summary = "Browse products",
            description = "Returns a paginated list of products. Page numbering starts at zero and defaults are configured in application properties.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products fetched successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid page or page size."),
            @ApiResponse(responseCode = "503", description = "Product data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    /**
     * Handles the get all products endpoint.
     */
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<ProductResponseDTO>>> getAllProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Products fetched successfully",
                productService.getAllProducts(
                        page == null ? paginationProperties.getDefaultPage() : page,
                        size == null ? paginationProperties.getDefaultPageSize() : size)));
    }

    @PostMapping("/search")
    @Operation(summary = "Search products", description = "Searches products using allowlisted fields and operators. "
            + "Filters use AND semantics; page is zero-based and size is limited to 100.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products searched successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid search criteria, pagination, sorting, or value type."),
            @ApiResponse(responseCode = "503", description = "Product data is temporarily unavailable.")
    })
    /**
     * Handles the search products endpoint.
     */
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<ProductResponseDTO>>> searchProducts(
            @RequestBody SearchRequestDTO<ProductField> request) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Products searched successfully",
                productService.searchProducts(request)));
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
    /**
     * Handles the get product by id endpoint.
     */
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
    /**
     * Handles the create product endpoint.
     */
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
    /**
     * Handles the create products endpoint.
     */
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
    /**
     * Handles the update products endpoint.
     */
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
    /**
     * Handles the update products endpoint.
     */
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> updateProducts(
            @RequestBody List<ProductRequestDTO> productRequestDTOs) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Products updated successfully",
                productService.updateProducts(productRequestDTOs)));
    }

    @PostMapping(value = "/import", consumes = "multipart/form-data")
    @Operation(summary = "Import products from Excel",
            description = "Reads product rows from an .xlsx file. Rows with an ID are updated; rows without an ID are created. "
                    + "All rows are validated before any row is persisted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products imported successfully."),
            @ApiResponse(responseCode = "400", description = "The file is empty, malformed, or has unsupported columns/values."),
            @ApiResponse(responseCode = "422", description = "One or more product rows failed validation."),
            @ApiResponse(responseCode = "503", description = "Product data is temporarily unavailable.")
    })
    public ResponseEntity<ApiResponseDTO<List<ProductResponseDTO>>> importProducts(
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(new ApiResponseDTO<>(true, "Products imported successfully",
                productService.importProducts(productExcelParser.parse(file))));
    }

    @DeleteMapping("/delete-all")
    @Operation(summary = "Delete all products",
            description = "Permanently removes every product from the catalog. Use this operation with caution.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All products deleted successfully."),
            @ApiResponse(responseCode = "503", description = "Product data is temporarily unavailable."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    /**
     * Handles the delete all product endpoint.
     */
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
    /**
     * Handles the delete product endpoint.
     */
    public ResponseEntity<ApiResponseDTO<Void>> deleteProduct(
            @PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(new ApiResponseDTO<>(true,
                "Product deleted successfully", null));
    }

}

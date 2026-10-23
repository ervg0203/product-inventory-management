package com.example.inventory.controller;

import com.example.inventory.dto.request.ProductRequestDTO;
import com.example.inventory.dto.common.SearchRequestDTO;
import com.example.inventory.dto.common.ProductSearchQueryDTO;
import com.example.inventory.enums.ProductField;
import com.example.inventory.dto.common.ApiResponseDTO;
import com.example.inventory.dto.common.ImportResponseDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.dto.common.PageResponseDTO;
import com.example.inventory.service.ProductService;
import com.example.inventory.config.PaginationProperties;
import com.example.inventory.util.ProductExcelParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @PostMapping("/advance-search")
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

    @GetMapping("/search")
    @Operation(summary = "Search products with query parameters",
            description = "Searches products using AND semantics for supplied filters and OR semantics across "
                    + "title, description, SKU, brand, and category for query. If no filters are supplied, "
                    + "this returns a paginated product listing. Range and explicit comparison filters for the "
                    + "same numeric field cannot be combined.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products searched successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid search criteria, pagination, sorting, or value type."),
            @ApiResponse(responseCode = "503", description = "Product data is temporarily unavailable.")
    })
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<ProductResponseDTO>>> searchProductsByQuery(
            @Parameter(description = "Keyword across title, description, SKU, brand, and category.",
                    example = "phone")
            @RequestParam(required = false) String query,
            @Parameter(description = "Partial, case-insensitive title match.", example = "Galaxy")
            @RequestParam(required = false) String title,
            @Parameter(description = "Partial, case-insensitive description match.", example = "wireless")
            @RequestParam(required = false) String description,
            @Parameter(description = "Exact SKU match.", example = "SKU-1001")
            @RequestParam(required = false) String sku,
            @Parameter(description = "Exact brand match.", example = "Samsung")
            @RequestParam(required = false) String brand,
            @Parameter(description = "Exact category match.", example = "smartphones")
            @RequestParam(required = false) String category,
            @Parameter(description = "Exact return-policy match.", example = "30 days")
            @RequestParam(required = false) String returnPolicy,
            @Parameter(description = "Inclusive minimum price.", example = "500",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(type = "number"))
            @RequestParam(required = false) String minPrice,
            @Parameter(description = "Inclusive maximum price.", example = "1500",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(type = "number"))
            @RequestParam(required = false) String maxPrice,
            @Parameter(description = "Inclusive minimum rating.", example = "4",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(type = "number"))
            @RequestParam(required = false) String minRating,
            @Parameter(description = "Inclusive maximum rating.", example = "5",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(type = "number"))
            @RequestParam(required = false) String maxRating,
            @Parameter(description = "Inclusive minimum discount percentage.", example = "10",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(type = "number"))
            @RequestParam(required = false) String minDiscountPercentage,
            @Parameter(description = "Inclusive maximum discount percentage.", example = "50",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(type = "number"))
            @RequestParam(required = false) String maxDiscountPercentage,
            @Parameter(description = "Numeric price operator: EQUALS, NOT_EQUALS, GREATER_THAN, "
                    + "GREATER_THAN_OR_EQUAL, LESS_THAN, or LESS_THAN_OR_EQUAL.", example = "LESS_THAN")
            @RequestParam(required = false) String priceOperator,
            @Parameter(description = "Value paired with priceOperator.", example = "1000",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(type = "number"))
            @RequestParam(required = false) String priceValue,
            @Parameter(description = "Numeric rating operator.", example = "GREATER_THAN_OR_EQUAL")
            @RequestParam(required = false) String ratingOperator,
            @Parameter(description = "Value paired with ratingOperator.", example = "4",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(type = "number"))
            @RequestParam(required = false) String ratingValue,
            @Parameter(description = "Numeric discount operator.", example = "LESS_THAN")
            @RequestParam(required = false) String discountOperator,
            @Parameter(description = "Value paired with discountOperator.", example = "20",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(type = "number"))
            @RequestParam(required = false) String discountValue,
            @Parameter(description = "Comma-separated exact brand values.", example = "Samsung,Apple")
            @RequestParam(required = false) String brands,
            @Parameter(description = "Comma-separated exact category values.", example = "smartphones,tablets")
            @RequestParam(required = false) String categories,
            @Parameter(description = "Exactly two comma-separated inclusive price bounds.", example = "500,2000")
            @RequestParam(required = false) String priceBetween,
            @Parameter(description = "Exactly two comma-separated inclusive rating bounds.", example = "3,5")
            @RequestParam(required = false) String ratingBetween,
            @Parameter(description = "Zero-based page number.", example = "0", schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "0", minimum = "0"))
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size, from 1 through 100.", example = "10", schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "20", minimum = "1", maximum = "100"))
            @RequestParam(required = false) Integer size,
            @Parameter(description = "ProductEntity field to sort by.", example = "price")
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction: ASC or DESC.", example = "ASC")
            @RequestParam(required = false) String sortDirection) {
        ProductSearchQueryDTO request = new ProductSearchQueryDTO();
        request.setQuery(query);
        request.setTitle(title);
        request.setDescription(description);
        request.setSku(sku);
        request.setBrand(brand);
        request.setCategory(category);
        request.setReturnPolicy(returnPolicy);
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);
        request.setMinRating(minRating);
        request.setMaxRating(maxRating);
        request.setMinDiscountPercentage(minDiscountPercentage);
        request.setMaxDiscountPercentage(maxDiscountPercentage);
        request.setPriceOperator(priceOperator);
        request.setPriceValue(priceValue);
        request.setRatingOperator(ratingOperator);
        request.setRatingValue(ratingValue);
        request.setDiscountOperator(discountOperator);
        request.setDiscountValue(discountValue);
        request.setBrands(brands);
        request.setCategories(categories);
        request.setPriceBetween(priceBetween);
        request.setRatingBetween(ratingBetween);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortDirection(sortDirection);
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
                    + "All rows are validated before any row is persisted. The response separates saved products into created and updated lists.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products imported successfully."),
            @ApiResponse(responseCode = "400", description = "The file is empty, malformed, or has unsupported columns/values."),
            @ApiResponse(responseCode = "422", description = "One or more product rows failed validation."),
            @ApiResponse(responseCode = "503", description = "Product data is temporarily unavailable.")
    })
    public ResponseEntity<ApiResponseDTO<ImportResponseDTO<ProductResponseDTO>>> importProducts(
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

    @DeleteMapping("/delete-by-filters")
    @Operation(summary = "Delete products by filters",
            description = "Deletes all products matching the supplied search filters and their related inventory records.")
    public ResponseEntity<ApiResponseDTO<Void>> deleteProductsByFilters(
            @RequestBody SearchRequestDTO<ProductField> request) {
        productService.deleteProductsByFilters(request);
        return ResponseEntity.ok(new ApiResponseDTO<>(true,
                "Products matching filters deleted successfully", null));
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

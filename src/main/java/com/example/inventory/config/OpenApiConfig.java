package com.example.inventory.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
/**
 * Provides the open api config component.
 */
public class OpenApiConfig {

    @Bean
    /**
     * Returns the result of the product inventory open api operation.
 * @return operation result
     */
    public OpenAPI productInventoryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Inventory Management API")
                        .description(
                                """
                                A simple and reliable API for managing your product catalog.

                                Use this API to create products individually or in bulk, browse products with pagination,
                                view product details, and remove products when they are no longer needed.

                                Validation errors are returned with the affected field and a clear explanation.
                                Paginated responses include the current page, page size, total pages, and total records.
                                """
                        )
                        .version("v1"));
    }

    @Bean
    /**
     * Adds response examples to the error responses declared by controllers.
 * @return operation result
     */
    public OpenApiCustomizer errorResponseExamples() {
        return openAPI -> openAPI.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    if (operation.getResponses() == null) {
                        return;
                    }
                    operation.getResponses().forEach((status, response) -> {
                        if (!isDocumentedError(status) || "204".equals(status)) {
                            return;
                        }
                        String path = openAPI.getPaths().entrySet().stream()
                                .filter(entry -> entry.getValue() == pathItem)
                                .map(Map.Entry::getKey)
                                .findFirst()
                                .orElse("");
                        MediaType mediaType = new MediaType()
                                .schema(new ObjectSchema());
                        mediaType.addExamples("error",
                                new Example()
                                        .summary("Error response for " + status)
                                        .value(errorExample(status, path)));
                        response.setContent(new Content().addMediaType("application/json", mediaType));
                    });
                }));
    }
/**
 * Executes the is documented error operation.
 *
 * @param status input value
 * @return operation result
 */

    private boolean isDocumentedError(String status) {
        return "400".equals(status) || "404".equals(status) || "409".equals(status)
                || "422".equals(status) || "503".equals(status) || "500".equals(status);
    }
/**
 * Executes the error example operation.
 *
 * @param status input value
 * @param path input value
 * @return operation result
 */

    private Map<String, Object> errorExample(String status, String path) {
        String domain = path.contains("/products") ? "product"
                : path.contains("/warehouses") ? "warehouse" : "inventory";
        String message = switch (status) {
            case "400" -> "inventory".equals(domain)
                    ? ("low-stock".equals(path.substring(path.lastIndexOf('/') + 1))
                    ? "Threshold must be non-negative" : "Request validation failed")
                    : "Request is malformed or contains invalid parameters";
            case "404" -> switch (domain) {
                case "product" -> "Product not found with id: product-001";
                case "warehouse" -> path.endsWith("/low-stock")
                        ? "Warehouse not found with id: warehouse-001"
                        : "Warehouse not found with id: warehouse-001";
                default -> path.endsWith("/low-stock")
                        ? "Warehouse not found with id: warehouse-001"
                        : path.endsWith("/products/{productId}")
                        ? "Product not found with id: product-001"
                        : path.endsWith("/warehouses/{warehouseId}")
                        ? "Warehouse not found with id: warehouse-001"
                        : "Inventory not found with id: inventory-001";
            };
            case "409" -> switch (domain) {
                case "product" -> "A product with the same SKU already exists";
                case "warehouse" -> "A warehouse with the same code already exists";
                default -> path.endsWith("/create")
                        ? "Inventory already exists for this product and warehouse"
                        : path.endsWith("/stock/decrease")
                        ? "Insufficient stock for inventory: inventory-001"
                        : "Warehouse warehouse-001 is not active";
            };
            case "503" -> switch (domain) {
                default -> "Product data is temporarily unavailable";
            };
            case "500" -> "An unexpected error occurred";
            default -> "Request validation failed";
        };
        Object data = null;
        if ("400".equals(status)) {
            data = null;
        } else if ("422".equals(status)) {
            message = switch (domain) {
                case "product" -> "One or more products failed validation";
                case "warehouse" -> "One or more warehouses failed validation";
                default -> "One or more inventory records failed validation";
            };
            if ("inventory".equals(domain)) {
                data = java.util.List.of();
            } else {
                Map<String, Object> validation = new LinkedHashMap<>();
                validation.put("totalErrors", 1);
                validation.put("totalIncorrectDtos", 1);
                validation.put("incorrectDtos", java.util.List.of());
                data = validation;
            }
        }
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("success", false);
        example.put("message", message);
        example.put("data", data);
        return example;
    }
}
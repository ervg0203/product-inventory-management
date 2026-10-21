package com.example.inventory.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
/**
 * Provides the open api config component.
 */
public class OpenApiConfig {

    @Bean
    /**
     * Returns the result of the product inventory open api operation.
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
}
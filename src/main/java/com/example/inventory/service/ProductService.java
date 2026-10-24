package com.example.inventory.service;
import com.example.inventory.dto.request.ProductRequestDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.dto.common.ImportResponseDTO;
import com.example.inventory.dto.common.PageResponseDTO;
import com.example.inventory.dto.common.SearchRequestDTO;
import com.example.inventory.dto.common.ProductSearchQueryDTO;
import com.example.inventory.enums.ProductField;

import java.util.List;

public interface ProductService {

    PageResponseDTO<ProductResponseDTO> getAllProducts(int page, int size);

    PageResponseDTO<ProductResponseDTO> searchProducts(SearchRequestDTO<ProductField> request);

    PageResponseDTO<ProductResponseDTO> searchProducts(ProductSearchQueryDTO request);

    ProductResponseDTO getProductById(String id);

    ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO);
/**
 * Executes the create products operation.
 *
 * @param productRequestDTOS input value
 * @return operation result
 */

    List<ProductResponseDTO> createProducts(List<ProductRequestDTO> productRequestDTOS);

    ProductResponseDTO updateProduct(ProductRequestDTO productRequestDTOS);
/**
 * Executes the update products operation.
 *
 * @param productRequestDTOS input value
// * @param products input value
 * @return operation result
 */

    List<ProductResponseDTO> updateProducts(List<ProductRequestDTO> productRequestDTOS);

    ImportResponseDTO<ProductResponseDTO> importProducts(List<ProductRequestDTO> products);
/**
 * Executes the delete product operation.
 *
 * @param id input value
 */

    void deleteProduct(String id);
/**
 * Executes the delete products by filters operation.
 *
 * @param request input value
 */
    void deleteProductsByFilters(SearchRequestDTO<ProductField> request);
/**
 * Executes the delete all products operation.
 *
 */

    void deleteAllProducts();
}
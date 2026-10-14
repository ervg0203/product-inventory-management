package com.example.inventory.service.impl;

import com.example.inventory.dto.request.CreateProductRequestDTO;
import com.example.inventory.dto.request.UpdateProductRequestDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.entity.ProductEntity;
import com.example.inventory.mapper.ProductMapper;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponseDTO createProduct(
            CreateProductRequestDTO requestDTO) {
        if (productRepository.existsBySku(requestDTO.getSku())) {
            throw new IllegalArgumentException(
                    "A product with SKU '" + requestDTO.getSku()
                            + "' already exists"
            );
        }
        ProductEntity product = productMapper.toEntity(requestDTO);
        try {
            ProductEntity savedProduct = productRepository.save(product);
            return productMapper.toResponseDTO(savedProduct);
        } catch (DuplicateKeyException exception) {
            throw new IllegalArgumentException(
                    "A product with SKU '" + requestDTO.getSku()
                            + "' already exists",
                    exception
            );
        }
    }

    @Override
    public ProductResponseDTO getProductById(String id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product not found with ID: " + id
                ));
        return productMapper.toResponseDTO(product);
    }

    @Override
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponseDTO)
                .toList();
    }

    @Override
    public ProductResponseDTO updateProduct(
            String id,
            UpdateProductRequestDTO requestDTO) {
        ProductEntity existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product not found with ID: " + id
                ));
        boolean skuUsedByAnotherProduct =
                productRepository.existsBySkuAndIdNot(
                        requestDTO.getSku(), id
                );
        if (skuUsedByAnotherProduct) {
            throw new IllegalArgumentException(
                    "A product with SKU '" + requestDTO.getSku()
                            + "' already exists"
            );
        }
        productMapper.updateEntity(existingProduct, requestDTO);
        try {
            ProductEntity updatedProduct =
                    productRepository.save(existingProduct);
            return productMapper.toResponseDTO(updatedProduct);
        } catch (DuplicateKeyException exception) {
            throw new IllegalArgumentException(
                    "A product with SKU '" + requestDTO.getSku()
                            + "' already exists",
                    exception
            );
        }
    }

    @Override
    public void deleteProduct(String id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product not found with ID: " + id
                ));
        productRepository.delete(product);
    }
}
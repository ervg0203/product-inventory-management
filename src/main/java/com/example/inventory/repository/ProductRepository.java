package com.example.inventory.repository;

import com.example.inventory.entity.ProductEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<ProductEntity, String> {

    Optional<ProductEntity> findBySku(String sku);

    List<ProductEntity> findAll();

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, String id);
}

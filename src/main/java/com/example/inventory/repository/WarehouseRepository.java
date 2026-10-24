package com.example.inventory.repository;

import com.example.inventory.entity.WarehouseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WarehouseRepository extends MongoRepository<WarehouseEntity, String> {
/**
 * Executes the find all operation.
 *
 * @param pageable input value
 * @return operation result
 */

    Page<WarehouseEntity> findAll(Pageable pageable);
/**
 * Executes the find all by code in operation.
 *
 * @param codes input value
 * @return operation result
 */
    List<WarehouseEntity> findAllByCodeIn(Iterable<String> codes);
}

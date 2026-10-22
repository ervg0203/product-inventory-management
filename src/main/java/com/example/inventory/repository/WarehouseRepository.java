package com.example.inventory.repository;

import com.example.inventory.entity.WarehouseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WarehouseRepository extends MongoRepository<WarehouseEntity, String> {

    Page<WarehouseEntity> findAll(Pageable pageable);
    List<WarehouseEntity> findAllByCodeIn(Iterable<String> codes);
}

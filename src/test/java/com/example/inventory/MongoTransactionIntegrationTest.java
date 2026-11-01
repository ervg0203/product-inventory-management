package com.example.inventory;

import com.example.inventory.entity.InventoryEntity;
import com.example.inventory.entity.ProductEntity;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.ProductRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoTransactionIntegrationTest {

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:8.0")
            .withCommand("mongod --replSet rs0 --bind_ip_all");

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", () -> mongo.getReplicaSetUrl("inventory"));
        registry.add("spring.data.mongodb.auto-index-creation", () -> "true");
    }

    @BeforeAll
    void initializeReplicaSet() throws Exception {
        mongo.execInContainer("mongosh", "--eval", "rs.initiate()");
        Thread.sleep(1000);
    }

    @Test
    void rollsBackCrossCollectionDeletesWhenTransactionFails() {
        ProductEntity product = productRepository.save(ProductEntity.builder()
                .sku("transaction-product")
                .title("Transaction product")
                .build());
        inventoryRepository.save(InventoryEntity.builder()
                .productId(product.getId())
                .warehouseId("warehouse-1")
                .quantity(10)
                .build());

        try {
            transactionTemplate.executeWithoutResult(status -> {
                inventoryRepository.deleteByProductId(product.getId());
                productRepository.deleteById(product.getId());
                throw new IllegalStateException("force rollback");
            });
        } catch (IllegalStateException ignored) {
            // Expected: the transaction must roll back both collection changes.
        }

        assertEquals(1, productRepository.findById(product.getId()).stream().count());
        assertEquals(1, inventoryRepository.findByProductId(product.getId()).stream().count());

        mongoTemplate.dropCollection(ProductEntity.class);
        mongoTemplate.dropCollection(InventoryEntity.class);
    }
}

package com.example.inventory.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "inventory")
@CompoundIndex(
        name = "product_warehouse_unique",
        def = "{'productId': 1, 'warehouseId': 1}",
        unique = true
)
public class InventoryEntity {

    @Id
    private String id;

    private String productId;

    private String warehouseId;

    private Integer quantity;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
package com.example.inventory.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "warehouses")
public class WarehouseEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code;

    private String name;

    private String address;

    private String city;

    private String state;

    private String postalCode;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
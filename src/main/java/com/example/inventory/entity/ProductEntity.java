package com.example.inventory.entity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class ProductEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String sku;

    private String title;

    private String description;

    private String category;

    private BigDecimal price;

    private BigDecimal discountPercentage;

    private BigDecimal rating;

    private Integer stock;

    private String brand;

    private String availabilityStatus;

    private String returnPolicy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}


package com.example.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StockTransferRequestDTO {
    @NotBlank private String productId;
    @NotBlank private String sourceWarehouseId;
    @NotBlank private String targetWarehouseId;
    @NotNull @Positive private Integer quantity;
}

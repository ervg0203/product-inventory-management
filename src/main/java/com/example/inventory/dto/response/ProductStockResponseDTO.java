package com.example.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockResponseDTO {

    private String productId;
    private List<InventoryResponseDTO> inventory;
    private int totalStock;
}

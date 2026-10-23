package com.example.inventory.dto.response;

import com.example.inventory.dto.common.ErrorsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponseDTO {

    private String id;
    private String productId;
    private String warehouseId;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ErrorsDTO> errors;
    private boolean isValid;
}

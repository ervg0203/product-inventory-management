package com.example.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import com.example.inventory.dto.common.ErrorsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequestDTO {

    private String id;
    @NotBlank private String productId;
    @NotBlank private String warehouseId;
    @NotNull
    @PositiveOrZero
    private Integer quantity;

    @Builder.Default
    private List<ErrorsDTO> errors = new ArrayList<>();

    @Builder.Default
    private Boolean isValid = true;
}

package com.example.inventory.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StockAdjustmentRequestDTO {

    @NotNull(message = "Adjustment quantity is required")
    @Positive(message = "Adjustment quantity must be greater than zero")
    private Integer quantity;

    @NotBlank(message = "Adjustment reason is required")
    @Size(max = 200, message = "Reason cannot exceed 200 characters")
    private String reason;
}
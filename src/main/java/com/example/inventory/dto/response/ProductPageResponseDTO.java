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
public class ProductPageResponseDTO {

    private List<ProductResponseDTO> products;
    private long totalRecords;
    private int page;
    private int pageSize;
    private int totalPages;
}

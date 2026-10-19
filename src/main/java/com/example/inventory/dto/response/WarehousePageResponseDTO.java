package com.example.inventory.dto.response;

import lombok.*;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WarehousePageResponseDTO {

    private List<WarehouseResponseDTO> warehouses;
    private long totalRecords;
    private int page;
    private int pageSize;
    private int totalPages;
}

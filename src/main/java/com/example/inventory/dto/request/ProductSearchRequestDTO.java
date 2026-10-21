package com.example.inventory.dto.request;

import com.example.inventory.enums.SortDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequestDTO {
    private List<ProductFilterDTO> filters;
    private Integer page;
    private Integer size;
    private String sortBy;
    private SortDirection sortDirection;
}

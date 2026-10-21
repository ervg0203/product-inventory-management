package com.example.inventory.dto.request;

import com.example.inventory.enums.ProductField;
import com.example.inventory.enums.SearchOperator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterDTO {
    private ProductField field;
    private SearchOperator operator;
    private Object value;
}

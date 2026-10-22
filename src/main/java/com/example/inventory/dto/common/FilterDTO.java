package com.example.inventory.dto.common;

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
public class FilterDTO<F> {
    private F field;
    private SearchOperator operator;
    private Object value;
}

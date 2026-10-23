package com.example.inventory.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
/**
 * Provides the errors component.
 */
public class ErrorsDTO {

    private String field;
    private String message;
}

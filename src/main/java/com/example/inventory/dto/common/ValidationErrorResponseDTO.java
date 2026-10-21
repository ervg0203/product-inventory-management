package com.example.inventory.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Contains invalid DTOs together with aggregate validation counts.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponseDTO<T> {

    private int totalErrors;
    private int totalIncorrectDtos;
    private List<T> incorrectDtos;
}

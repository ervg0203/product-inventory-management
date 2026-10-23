package com.example.inventory.dto.response;

import com.example.inventory.dto.common.ErrorsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseResponseDTO {

    private String id;

    private String code;

    private String name;

    private String address;

    private String city;

    private String state;

    private String postalCode;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<ErrorsDTO> errors;

    private boolean isValid;
}

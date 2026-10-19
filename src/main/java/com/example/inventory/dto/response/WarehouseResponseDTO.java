package com.example.inventory.dto.response;

import com.example.inventory.util.Errors;
import lombok.*;

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
    private List<Errors> errors;
    private boolean isValid;
}

package com.example.inventory.dto.request;

import com.example.inventory.util.Errors;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseRequestDTO {

    private String id;
    private String code;
    private String name;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private boolean active;

    @Builder.Default
    private List<Errors> errors = new ArrayList<>();

    @Builder.Default
    private Boolean isValid = true;
}

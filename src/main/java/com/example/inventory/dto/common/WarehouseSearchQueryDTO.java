package com.example.inventory.dto.common;

import lombok.Getter;
import lombok.Setter;

/**
 * Query parameters accepted by the user-friendly warehouse search endpoint.
 */
@Getter
@Setter
public class WarehouseSearchQueryDTO {

    private String query;
    private String code;
    private String name;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private Boolean active;
    private String codes;
    private String names;
    private String cities;
    private String states;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}

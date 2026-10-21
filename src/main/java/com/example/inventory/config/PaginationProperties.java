package com.example.inventory.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "inventory.pagination")
/**
 * Provides the pagination properties component.
 */
public class PaginationProperties {

    private int defaultPage = 0;
    private int defaultPageSize = 20;
    private int maxPageSize = 100;
}

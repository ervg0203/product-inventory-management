package com.example.inventory.util;

import com.example.inventory.dto.common.FilterDTO;
import com.example.inventory.dto.common.ProductSearchQueryDTO;
import com.example.inventory.dto.common.SearchRequestDTO;
import com.example.inventory.dto.common.WarehouseSearchQueryDTO;
import com.example.inventory.enums.ProductField;
import com.example.inventory.enums.SearchOperator;
import com.example.inventory.enums.SortDirection;
import com.example.inventory.enums.WarehouseField;
import org.springframework.stereotype.Component;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class SearchCriteriaBuilder {

    public SearchRequestDTO<ProductField> build(ProductSearchQueryDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request cannot be null");
        }

        if (request.getQuery() != null && request.getQuery().isBlank()) {
            throw new IllegalArgumentException("query cannot be blank");
        }
        if (request.getBrand() != null && request.getBrands() != null) {
            throw new IllegalArgumentException("Use either brand or brands, not both");
        }
        if (request.getCategory() != null && request.getCategories() != null) {
            throw new IllegalArgumentException("Use either category or categories, not both");
        }

        List<FilterDTO<ProductField>> filters = new ArrayList<>();
        addTextFilter(filters, ProductField.TITLE, request.getTitle(), SearchOperator.CONTAINS);
        addTextFilter(filters, ProductField.DESCRIPTION, request.getDescription(), SearchOperator.CONTAINS);
        addTextFilter(filters, ProductField.SKU, request.getSku(), SearchOperator.EQUALS);
        addTextFilter(filters, ProductField.BRAND, request.getBrand(), SearchOperator.EQUALS);
        addTextFilter(filters, ProductField.CATEGORY, request.getCategory(), SearchOperator.EQUALS);
        addTextFilter(filters, ProductField.RETURN_POLICY, request.getReturnPolicy(), SearchOperator.EQUALS);
        addListFilter(filters, ProductField.BRAND, request.getBrands());
        addListFilter(filters, ProductField.CATEGORY, request.getCategories());

        addRangeFilters(filters, ProductField.PRICE, request.getMinPrice(), request.getMaxPrice(),
                request.getPriceBetween(), request.getPriceOperator(), request.getPriceValue());
        addRangeFilters(filters, ProductField.RATING, request.getMinRating(), request.getMaxRating(),
                request.getRatingBetween(), request.getRatingOperator(), request.getRatingValue());
        addRangeFilters(filters, ProductField.DISCOUNT_PERCENTAGE, request.getMinDiscountPercentage(),
                request.getMaxDiscountPercentage(), null, request.getDiscountOperator(), request.getDiscountValue());

        SortDirection direction = parseSortDirection(request.getSortDirection());
        return SearchRequestDTO.<ProductField>builder()
                .filters(filters)
                .page(request.getPage())
                .size(request.getSize())
                .sortBy(validateSortField(request.getSortBy(), direction))
                .sortDirection(direction)
                .build();
    }

    private void addTextFilter(List<FilterDTO<ProductField>> filters, ProductField field, String value,
                               SearchOperator operator) {
        if (value != null) {
            filters.add(FilterDTO.<ProductField>builder().field(field).operator(operator).value(value).build());
        }
    }

    private void addListFilter(List<FilterDTO<ProductField>> filters, ProductField field, String value) {
        if (value != null) {
            filters.add(FilterDTO.<ProductField>builder().field(field).operator(SearchOperator.IN)
                    .value(parseList(value, field.getValue())).build());
        }

    }

    private void addRangeFilters(List<FilterDTO<ProductField>> filters, ProductField field, String min, String max,
                                 String between, String operatorText, String operatorValue) {
        if (between != null && (min != null || max != null || operatorText != null || operatorValue != null)
                || operatorText != null && (min != null || max != null)) {
            throw new IllegalArgumentException("Conflicting range filters supplied for " + field.getValue());
        }
        if ((operatorText == null) != (operatorValue == null)) {
            throw new IllegalArgumentException("Operator and value must be supplied together for " + field.getValue());
        }
        if (between != null) {
            List<String> bounds = parseList(between, field.getValue() + "Between");
            if (bounds.size() != 2) {
                throw new IllegalArgumentException(field.getValue() + "Between requires exactly two values");
            }
            filters.add(FilterDTO.<ProductField>builder().field(field).operator(SearchOperator.BETWEEN)
                    .value(bounds).build());
            return;
        }
        if (min != null && max != null) {
            filters.add(FilterDTO.<ProductField>builder().field(field).operator(SearchOperator.BETWEEN)
                    .value(List.of(min, max)).build());
        } else if (min != null) {
            filters.add(numericFilter(field, SearchOperator.GREATER_THAN_OR_EQUAL, min));
        } else if (max != null) {
            filters.add(numericFilter(field, SearchOperator.LESS_THAN_OR_EQUAL, max));
        }
        if (operatorText != null) {
            SearchOperator operator = SearchOperator.fromValue(operatorText);
            if (operator == SearchOperator.CONTAINS || operator == SearchOperator.STARTS_WITH
                    || operator == SearchOperator.ENDS_WITH || operator == SearchOperator.IN
                    || operator == SearchOperator.BETWEEN) {
                throw new IllegalArgumentException("Unsupported numeric operator: " + operatorText);
            }
            filters.add(numericFilter(field, operator, operatorValue));
        }
    }

    private FilterDTO<ProductField> numericFilter(ProductField field, SearchOperator operator, String value) {
        return FilterDTO.<ProductField>builder().field(field).operator(operator)
                .value(parseNumber(value, field.getValue())).build();
    }

    private BigDecimal parseNumber(String value, String field) {
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException | NullPointerException exception) {
            throw new IllegalArgumentException("Invalid numeric value for " + field + ": " + value);
        }
    }

    private List<String> parseList(String value, String field) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
        List<String> values = Arrays.stream(value.split(",", -1)).map(String::trim).toList();
        if (values.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException(field + " contains an empty value");
        }
        return values;
    }

    private SortDirection parseSortDirection(String value) {
        if (value == null) {
            return null;
        }
        try {
            return SortDirection.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported sort direction: " + value);
        }
    }

    private String validateSortField(String value, SortDirection direction) {
        if (value == null) {
            if (direction != null) {
                throw new IllegalArgumentException("sortDirection requires sortBy");
            }
            return null;
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("sortBy cannot be blank");
        }
        return ProductField.fromValue(value).getValue();
    }

    public SearchRequestDTO<WarehouseField> build(WarehouseSearchQueryDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request cannot be null");
        }
        if (request.getQuery() != null && request.getQuery().isBlank()) {
            throw new IllegalArgumentException("query cannot be blank");
        }
        List<FilterDTO<WarehouseField>> filters = new ArrayList<>();
        addWarehouseTextFilter(filters, WarehouseField.CODE, request.getCode(), SearchOperator.EQUALS);
        addWarehouseTextFilter(filters, WarehouseField.NAME, request.getName(), SearchOperator.CONTAINS);
        addWarehouseTextFilter(filters, WarehouseField.ADDRESS, request.getAddress(), SearchOperator.CONTAINS);
        addWarehouseTextFilter(filters, WarehouseField.CITY, request.getCity(), SearchOperator.CONTAINS);
        addWarehouseTextFilter(filters, WarehouseField.STATE, request.getState(), SearchOperator.CONTAINS);
        addWarehouseTextFilter(filters, WarehouseField.POSTAL_CODE, request.getPostalCode(), SearchOperator.CONTAINS);
        if (request.getActive() != null) {
            filters.add(FilterDTO.<WarehouseField>builder().field(WarehouseField.ACTIVE)
                    .operator(SearchOperator.EQUALS).value(request.getActive()).build());
        }
        addWarehouseListFilter(filters, WarehouseField.CODE, request.getCodes());
        addWarehouseListFilter(filters, WarehouseField.NAME, request.getNames());
        addWarehouseListFilter(filters, WarehouseField.CITY, request.getCities());
        addWarehouseListFilter(filters, WarehouseField.STATE, request.getStates());

        SortDirection direction = parseSortDirection(request.getSortDirection());
        return SearchRequestDTO.<WarehouseField>builder().filters(filters).page(request.getPage())
                .size(request.getSize()).sortBy(validateWarehouseSortField(request.getSortBy(), direction))
                .sortDirection(direction).build();
    }

    public void addWarehouseKeywordCriteria(Query query, String keyword) {
        if (keyword == null) {
            return;
        }
        String text = Pattern.quote(keyword);
        query.addCriteria(new Criteria().orOperator(
                Criteria.where(WarehouseField.CODE.getValue()).regex(".*" + text + ".*", "i"),
                Criteria.where(WarehouseField.NAME.getValue()).regex(".*" + text + ".*", "i"),
                Criteria.where(WarehouseField.ADDRESS.getValue()).regex(".*" + text + ".*", "i"),
                Criteria.where(WarehouseField.CITY.getValue()).regex(".*" + text + ".*", "i"),
                Criteria.where(WarehouseField.STATE.getValue()).regex(".*" + text + ".*", "i"),
                Criteria.where(WarehouseField.POSTAL_CODE.getValue()).regex(".*" + text + ".*", "i")));
    }

    private void addWarehouseTextFilter(List<FilterDTO<WarehouseField>> filters, WarehouseField field,
                                        String value, SearchOperator operator) {
        if (value != null) {
            filters.add(FilterDTO.<WarehouseField>builder().field(field).operator(operator).value(value).build());
        }
    }

    private void addWarehouseListFilter(List<FilterDTO<WarehouseField>> filters, WarehouseField field,
                                        String value) {
        if (value != null) {
            if (value.isBlank()) {
                throw new IllegalArgumentException(field.getValue() + "s cannot be blank");
            }
            List<String> values = parseList(value, field.getValue() + "s");
            filters.add(FilterDTO.<WarehouseField>builder().field(field).operator(SearchOperator.IN)
                    .value(values).build());
        }
    }

    private String validateWarehouseSortField(String value, SortDirection direction) {
        if (value == null) {
            if (direction != null) {
                throw new IllegalArgumentException("sortDirection requires sortBy");
            }
            return null;
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("sortBy cannot be blank");
        }
        WarehouseField field = WarehouseField.fromValue(value);
        if (field == WarehouseField.ID) {
            throw new IllegalArgumentException("Unsupported warehouse sort field: " + value);
        }
        return field.getValue();
    }

}

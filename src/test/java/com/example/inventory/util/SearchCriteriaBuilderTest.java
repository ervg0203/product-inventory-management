package com.example.inventory.util;

import com.example.inventory.dto.common.*;
import com.example.inventory.enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SearchCriteriaBuilderTest {
    private final SearchCriteriaBuilder builder = new SearchCriteriaBuilder();

    private void assertInvalid(ProductSearchQueryDTO request) {
        assertThrows(IllegalArgumentException.class, () -> builder.build(request));
    }

    private void assertInvalid(WarehouseSearchQueryDTO request) {
        assertThrows(IllegalArgumentException.class, () -> builder.build(request));
    }

    @Test
    void buildsProductTextListRangeAndSortCriteria() {
        ProductSearchQueryDTO request = new ProductSearchQueryDTO();
        request.setTitle("phone"); request.setBrands("A, B"); request.setMinPrice("10");
        request.setMaxPrice("20"); request.setSortBy("price"); request.setSortDirection("desc");
        SearchRequestDTO<ProductField> result = builder.build(request);
        assertEquals(3, result.getFilters().size());
        assertEquals(ProductField.PRICE, result.getFilters().getLast().getField());
        assertEquals(SearchOperator.BETWEEN, result.getFilters().getLast().getOperator());
        assertEquals(SortDirection.DESC, result.getSortDirection());
    }

    @Test
    void rejectsConflictsMalformedNumbersAndSort() {
        ProductSearchQueryDTO request = new ProductSearchQueryDTO();
        request.setBrand("A"); request.setBrands("A,B");
        assertInvalid(request);
        request = new ProductSearchQueryDTO(); request.setPriceBetween("1");
        assertInvalid(request);
        request = new ProductSearchQueryDTO(); request.setPriceValue("1");
        assertInvalid(request);
        request = new ProductSearchQueryDTO(); request.setSortDirection("sideways");
        assertInvalid(request);
    }

    @Test
    void buildsWarehouseFiltersAndRejectsIdSort() {
        WarehouseSearchQueryDTO request = new WarehouseSearchQueryDTO();
        request.setActive(true); request.setCodes("A,B"); request.setSortBy("name");
        SearchRequestDTO<WarehouseField> result = builder.build(request);
        assertEquals(2, result.getFilters().size());
        assertEquals(WarehouseField.ACTIVE, result.getFilters().get(0).getField());
        request.setSortBy("id");
        assertThrows(IllegalArgumentException.class, () -> builder.build(request));
    }

    @Test
    void rejectsBlankQueriesAndListMembers() {
        ProductSearchQueryDTO request = new ProductSearchQueryDTO(); request.setQuery(" ");
        assertInvalid(request);
        request = new ProductSearchQueryDTO(); request.setBrands("A,,B");
        assertInvalid(request);
    }

    @Test
    void coversNumericOperatorsWarehouseKeywordAndSortValidation() {
        ProductSearchQueryDTO product = new ProductSearchQueryDTO();
        product.setPriceOperator("GREATER_THAN"); product.setPriceValue("10");
        product.setMinRating("3"); product.setMaxDiscountPercentage("20");
        product.setSortBy("sku");
        assertEquals(3, builder.build(product).getFilters().size());
        product.setPriceOperator("CONTAINS");
        assertInvalid(product);
        product = new ProductSearchQueryDTO(); product.setSortBy("");
        assertInvalid(product);
        product = new ProductSearchQueryDTO(); product.setSortDirection("ASC");
        assertInvalid(product);

        WarehouseSearchQueryDTO warehouse = new WarehouseSearchQueryDTO();
        warehouse.setName("Central"); warehouse.setCities("London,Paris");
        warehouse.setSortDirection("ASC"); warehouse.setSortBy("city");
        assertEquals(2, builder.build(warehouse).getFilters().size());
        warehouse.setCities(" ");
        assertInvalid(warehouse);
    }

    @Test
    void coversWarehouseKeywordCriteriaAndBooleanFilter() {
        Query query = new Query();
        builder.addWarehouseKeywordCriteria(query, "A+B");
        assertFalse(query.getQueryObject().isEmpty());
        builder.addWarehouseKeywordCriteria(query, null);
        WarehouseSearchQueryDTO warehouse = new WarehouseSearchQueryDTO();
        warehouse.setActive(false);
        assertEquals(Boolean.FALSE, builder.build(warehouse).getFilters().getFirst().getValue());
    }
}

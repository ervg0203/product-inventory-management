package com.example.inventory.util;

import com.example.inventory.dto.common.*;
import com.example.inventory.enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Query;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SearchQueryBuilderTest {
    private final SearchQueryBuilder builder = new SearchQueryBuilder();

    private void assertInvalid(FilterDTO<ProductField> filter) {
        assertThrows(IllegalArgumentException.class,
                () -> builder.build(SearchRequestDTO.<ProductField>builder().filters(List.of(filter)).build()));
    }

    @Test
    void supportsTextNumericInAndBetweenOperators() {
        SearchRequestDTO<ProductField> request = SearchRequestDTO.<ProductField>builder().filters(List.of(
                FilterDTO.<ProductField>builder().field(ProductField.TITLE).operator(SearchOperator.CONTAINS).value("phone").build(),
                FilterDTO.<ProductField>builder().field(ProductField.PRICE).operator(SearchOperator.BETWEEN).value(List.of("10", "20")).build(),
                FilterDTO.<ProductField>builder().field(ProductField.BRAND).operator(SearchOperator.IN).value(List.of("A", "B")).build()
        )).sortBy("price").sortDirection(SortDirection.DESC).build();
        Query query = builder.build(request, "x+y");
        assertNotNull(query.getQueryObject());
        assertFalse(query.getSortObject().isEmpty());
    }

    @Test
    void rejectsInvalidFiltersAndBounds() {
        FilterDTO<ProductField> missing = FilterDTO.<ProductField>builder().build();
        assertInvalid(missing);
        FilterDTO<ProductField> bad = FilterDTO.<ProductField>builder().field(ProductField.PRICE)
                .operator(SearchOperator.BETWEEN).value(List.of(new BigDecimal("2"), new BigDecimal("1"))).build();
        assertInvalid(bad);
        bad = FilterDTO.<ProductField>builder().field(ProductField.TITLE).operator(SearchOperator.GREATER_THAN).value("x").build();
        assertInvalid(bad);
    }

    @Test
    void convertsWarehouseBooleanAndRejectsInvalidBoolean() {
        FilterDTO<WarehouseField> active = FilterDTO.<WarehouseField>builder().field(WarehouseField.ACTIVE)
                .operator(SearchOperator.EQUALS).value("true").build();
        assertNotNull(builder.build(SearchRequestDTO.<WarehouseField>builder().filters(List.of(active)).build()));
        active.setValue("maybe");
        assertThrows(IllegalArgumentException.class, () -> builder.build(SearchRequestDTO.<WarehouseField>builder().filters(List.of(active)).build()));
    }

    @Test
    void coversAllTextOperatorsNumericConversionsAndSortNames() {
        for (SearchOperator operator : List.of(SearchOperator.STARTS_WITH,
                SearchOperator.ENDS_WITH, SearchOperator.EQUALS, SearchOperator.NOT_EQUALS)) {
            FilterDTO<ProductField> filter = FilterDTO.<ProductField>builder()
                    .field(ProductField.TITLE).operator(operator).value("phone").build();
            assertNotNull(builder.build(SearchRequestDTO.<ProductField>builder()
                    .filters(List.of(filter)).build()));
        }
        for (SearchOperator operator : List.of(SearchOperator.EQUALS, SearchOperator.NOT_EQUALS,
                SearchOperator.GREATER_THAN, SearchOperator.GREATER_THAN_OR_EQUAL,
                SearchOperator.LESS_THAN, SearchOperator.LESS_THAN_OR_EQUAL)) {
            FilterDTO<ProductField> filter = FilterDTO.<ProductField>builder()
                    .field(ProductField.PRICE).operator(operator).value("10.5").build();
            assertNotNull(builder.build(SearchRequestDTO.<ProductField>builder()
                    .filters(List.of(filter)).build()));
        }
        FilterDTO<WarehouseField> names = FilterDTO.<WarehouseField>builder().field(WarehouseField.NAME)
                .operator(SearchOperator.NOT_EQUALS).value("North").build();
        assertNotNull(builder.build(SearchRequestDTO.<WarehouseField>builder().filters(List.of(names))
                .sortBy("NAME").sortDirection(SortDirection.ASC).build()));
    }

    @Test
    void rejectsUnsupportedFieldsValuesAndCollections() {
        assertInvalid(FilterDTO.<ProductField>builder().field(ProductField.TITLE)
                .operator(SearchOperator.IN).value("x").build());
        assertInvalid(FilterDTO.<ProductField>builder().field(ProductField.PRICE)
                .operator(SearchOperator.BETWEEN).value(List.of("1")).build());
        assertInvalid(FilterDTO.<ProductField>builder().field(ProductField.PRICE)
                .operator(SearchOperator.EQUALS).value("bad").build());
        assertInvalid(FilterDTO.<ProductField>builder().field(ProductField.ID)
                .operator(SearchOperator.EQUALS).value("x").build());
        FilterDTO<WarehouseField> id = FilterDTO.<WarehouseField>builder().field(WarehouseField.ID)
                .operator(SearchOperator.EQUALS).value("x").build();
        assertThrows(IllegalArgumentException.class, () -> builder.build(
                SearchRequestDTO.<WarehouseField>builder().filters(List.of(id)).build()));
    }
}

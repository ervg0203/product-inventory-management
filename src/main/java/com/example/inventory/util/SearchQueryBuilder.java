package com.example.inventory.util;

import com.example.inventory.dto.common.FilterDTO;
import com.example.inventory.dto.common.SearchRequestDTO;
import com.example.inventory.enums.ProductField;
import com.example.inventory.enums.SearchOperator;
import com.example.inventory.enums.SortDirection;
import com.example.inventory.enums.WarehouseField;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

@Component
/**
 * Provides the search query builder component.
 * @param request input value
 * @return operation result
 */
public class SearchQueryBuilder {

    /**
     * Returns the result of the build operation.
 * @param request input value
// * @param keyword input value
 * @return operation result
     */
    public <F> Query build(SearchRequestDTO<F> request) {
        return build(request, null);
    }

    public <F> Query build(SearchRequestDTO<F> request, String keyword) {
        Query query = new Query();
        if (request.getFilters() != null) {
            request.getFilters().forEach(filter -> query.addCriteria(toCriteria(filter)));
        }
        if (keyword != null && !keyword.isBlank()) {
            String text = Pattern.quote(keyword);
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where(ProductField.TITLE.getValue()).regex(".*" + text + ".*", "i"),
                    Criteria.where(ProductField.DESCRIPTION.getValue()).regex(".*" + text + ".*", "i"),
                    Criteria.where(ProductField.SKU.getValue()).regex(".*" + text + ".*", "i"),
                    Criteria.where(ProductField.BRAND.getValue()).regex(".*" + text + ".*", "i"),
                    Criteria.where(ProductField.CATEGORY.getValue()).regex(".*" + text + ".*", "i")));
        }
        if (request.getSortBy() != null && !request.getSortBy().isBlank()) {
            String field = searchFieldFor(request.getSortBy(), request.getFilters());
            Sort.Direction direction = request.getSortDirection() == SortDirection.DESC
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            query.with(Sort.by(direction, field));
        }
        return query;
    }
/**
 * Executes the search field for operation.
 *
 * @param value input value
 * @param filters input value
 * @return operation result
 */

    private <F> String searchFieldFor(String value, List<FilterDTO<F>> filters) {
        boolean warehouseSearch = filters != null && filters.stream()
                .anyMatch(filter -> filter != null && filter.getField() instanceof WarehouseField);
        if (warehouseSearch) {
            return warehouseSearchFieldFor(value).getValue();
        }
        for (ProductField field : ProductField.values()) {
            if (isRestricted(field)) {
                continue;
            }
            if (field.getValue().equals(value) || field.name().equalsIgnoreCase(value)) {
                return field.getValue();
            }
        }
        return warehouseSearchFieldFor(value).getValue();
    }
/**
 * Executes the to criteria operation.
 *
 * @param filter input value
 * @return operation result
 */

    private Criteria toCriteria(FilterDTO<?> filter) {
        if (filter != null && filter.getField() instanceof WarehouseField) {
            return toWarehouseCriteria(filter);
        }
        return toProductCriteria(filter);
    }
/**
 * Executes the to product criteria operation.
 *
 * @param filter input value
 * @return operation result
 */

    private Criteria toProductCriteria(FilterDTO<?> filter) {
        if (filter == null || filter.getField() == null || filter.getOperator() == null
                || filter.getValue() == null) {
            throw new IllegalArgumentException("Each filter requires field, operator and value");
        }

        if (!(filter.getField() instanceof ProductField)) {
            throw new IllegalArgumentException("Unsupported product filter field");
        }
        ProductField field = (ProductField) filter.getField();
        if (isRestricted(field)) {
            throw new IllegalArgumentException("Unsupported product filter field: " + field.getValue());
        }
        SearchOperator operator = filter.getOperator();
        Class<?> type = fieldType(field);
        Object value = filter.getValue();
        if (value instanceof String text && text.isBlank()) {
            throw new IllegalArgumentException("Filter value cannot be blank");
        }
        if ((operator == SearchOperator.IN || operator == SearchOperator.BETWEEN)
                && !(value instanceof Collection<?>)) {
            throw new IllegalArgumentException(operator + " requires an array value");
        }
        if (operator == SearchOperator.BETWEEN && ((Collection<?>) value).size() != 2) {
            throw new IllegalArgumentException("BETWEEN requires exactly two values");
        }
        if ((operator == SearchOperator.IN || operator == SearchOperator.BETWEEN)
                && ((Collection<?>) value).isEmpty()) {
            throw new IllegalArgumentException(operator + " requires at least one value");
        }
        if (isTextOperator(operator) && type != String.class && operator != SearchOperator.EQUALS) {
            throw new IllegalArgumentException(operator + " is only compatible with text fields");
        }
        if ((operator == SearchOperator.GREATER_THAN || operator == SearchOperator.GREATER_THAN_OR_EQUAL
                || operator == SearchOperator.LESS_THAN || operator == SearchOperator.LESS_THAN_OR_EQUAL
                || operator == SearchOperator.BETWEEN) && type == String.class) {
            throw new IllegalArgumentException(operator + " is only compatible with numeric fields");
        }

        Criteria criteria = Criteria.where(field.getValue());
        if (isTextOperator(operator) && type == String.class) {
            String text = Pattern.quote(String.valueOf(value));
            String regex = switch (operator) {
                case CONTAINS -> ".*" + text + ".*";
                case STARTS_WITH -> "^" + text;
                case ENDS_WITH -> text + "$";
                case EQUALS -> "^" + text + "$";
                default -> throw new IllegalArgumentException("Unsupported text operator");
            };
            return criteria.regex(regex, "i");
        }
        if (operator == SearchOperator.NOT_EQUALS && type == String.class) {
            return criteria.not().regex("^" + Pattern.quote(String.valueOf(value)) + "$", "i");
        }
        if (operator == SearchOperator.IN) {
            return criteria.in(((Collection<?>) value).stream().map(item -> convert(item, type)).toList());
        }
        if (operator == SearchOperator.BETWEEN) {
            List<?> values = ((Collection<?>) value).stream().map(item -> convert(item, type)).toList();
            if (((BigDecimal) values.get(0)).compareTo((BigDecimal) values.get(1)) > 0) {
                throw new IllegalArgumentException("BETWEEN lower bound cannot exceed upper bound");
            }
            return criteria.gte(values.get(0)).lte(values.get(1));
        }
        Object converted = convert(value, type);
        return switch (operator) {
            case EQUALS -> criteria.is(converted);
            case NOT_EQUALS -> criteria.ne(converted);
            case GREATER_THAN -> criteria.gt(converted);
            case GREATER_THAN_OR_EQUAL -> criteria.gte(converted);
            case LESS_THAN -> criteria.lt(converted);
            case LESS_THAN_OR_EQUAL -> criteria.lte(converted);
            default -> throw new IllegalArgumentException("Unsupported operator");
        };
    }
/**
 * Executes the warehouse search field for operation.
 *
 * @param value input value
 * @return operation result
 */

    private WarehouseField warehouseSearchFieldFor(String value) {
        for (WarehouseField field : WarehouseField.values()) {
            if (field != WarehouseField.ID
                    && (field.getValue().equals(value) || field.name().equalsIgnoreCase(value))) {
                return field;
            }
        }
        throw new IllegalArgumentException("Unsupported warehouse search or sort field: " + value);
    }
/**
 * Executes the to warehouse criteria operation.
 *
 * @param filter input value
 * @return operation result
 */

    private Criteria toWarehouseCriteria(FilterDTO<?> filter) {
        if (filter == null || filter.getField() == null || filter.getOperator() == null
                || filter.getValue() == null) {
            throw new IllegalArgumentException("Each filter requires field, operator and value");
        }
        if (!(filter.getField() instanceof WarehouseField)) {
            throw new IllegalArgumentException("Unsupported warehouse filter field");
        }
        WarehouseField field = (WarehouseField) filter.getField();
        if (field == WarehouseField.ID) {
            throw new IllegalArgumentException("Unsupported warehouse filter field: " + field.getValue());
        }
        SearchOperator operator = filter.getOperator();
        Class<?> type = field == WarehouseField.ACTIVE ? Boolean.class : String.class;
        Object value = filter.getValue();
        if (value instanceof String text && text.isBlank()) {
            throw new IllegalArgumentException("Filter value cannot be blank");
        }
        if ((operator == SearchOperator.IN || operator == SearchOperator.BETWEEN)
                && !(value instanceof Collection<?>)) {
            throw new IllegalArgumentException(operator + " requires an array value");
        }
        if (operator == SearchOperator.BETWEEN) {
            throw new IllegalArgumentException("BETWEEN is only compatible with numeric fields");
        }
        if (operator == SearchOperator.IN && ((Collection<?>) value).isEmpty()) {
            throw new IllegalArgumentException("IN requires at least one value");
        }
        if (isTextOperator(operator) && type != String.class && operator != SearchOperator.EQUALS) {
            throw new IllegalArgumentException(operator + " is only compatible with text fields");
        }

        Criteria criteria = Criteria.where(field.getValue());
        if (isTextOperator(operator) && type == String.class) {
            String text = Pattern.quote(String.valueOf(value));
            String regex = switch (operator) {
                case CONTAINS -> ".*" + text + ".*";
                case STARTS_WITH -> "^" + text;
                case ENDS_WITH -> text + "$";
                case EQUALS -> "^" + text + "$";
                default -> throw new IllegalArgumentException("Unsupported text operator");
            };
            return criteria.regex(regex, "i");
        }
        if (operator == SearchOperator.NOT_EQUALS && type == String.class) {
            return criteria.not().regex("^" + Pattern.quote(String.valueOf(value)) + "$", "i");
        }
        if (operator == SearchOperator.IN) {
            return criteria.in(((Collection<?>) value).stream().map(item -> convertWarehouse(item, type)).toList());
        }
        Object converted = convertWarehouse(value, type);
        return switch (operator) {
            case EQUALS -> criteria.is(converted);
            case NOT_EQUALS -> criteria.ne(converted);
            default -> throw new IllegalArgumentException(operator + " is not compatible with this warehouse field");
        };
    }
/**
 * Executes the convert warehouse operation.
 *
 * @param value input value
 * @param type input value
 * @return operation result
 */

    private Object convertWarehouse(Object value, Class<?> type) {
        if (type == String.class) {
            return String.valueOf(value);
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if ("true".equalsIgnoreCase(String.valueOf(value))) {
            return true;
        }
        if ("false".equalsIgnoreCase(String.valueOf(value))) {
            return false;
        }
        throw new IllegalArgumentException("Invalid boolean value: " + value);
    }
/**
 * Executes the is restricted operation.
 *
 * @param field input value
 * @return operation result
 */

    private boolean isRestricted(ProductField field) {
        return field == ProductField.ID || field == ProductField.CREATED_AT || field == ProductField.UPDATED_AT;
    }
/**
 * Executes the is text operator operation.
 *
 * @param operator input value
 * @return operation result
 */

    private boolean isTextOperator(SearchOperator operator) {
        return operator == SearchOperator.CONTAINS || operator == SearchOperator.STARTS_WITH
                || operator == SearchOperator.ENDS_WITH || operator == SearchOperator.EQUALS;
    }
/**
 * Executes the field type operation.
 *
 * @param field input value
 * @return operation result
 */

    private Class<?> fieldType(ProductField field) {
        return switch (field) {
            case PRICE, DISCOUNT_PERCENTAGE, RATING -> BigDecimal.class;
            default -> String.class;
        };
    }
/**
 * Executes the convert operation.
 *
 * @param value input value
 * @param type input value
 * @return operation result
 */

    private Object convert(Object value, Class<?> type) {
        if (value == null) {
            throw new IllegalArgumentException("Filter value cannot be null");
        }
        if (type == String.class) {
            return String.valueOf(value);
        }
        try {
            if (type == BigDecimal.class) {
                return new BigDecimal(String.valueOf(value));
            }
            if (type == LocalDateTime.class) {
                return LocalDateTime.parse(String.valueOf(value));
            }
        } catch (NumberFormatException | DateTimeParseException exception) {
            throw new IllegalArgumentException("Invalid value for " + type.getSimpleName() + ": " + value);
        }
        return value;
    }
}

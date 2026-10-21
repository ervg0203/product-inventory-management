package com.example.inventory.util;

import com.example.inventory.dto.request.ProductFilterDTO;
import com.example.inventory.dto.request.ProductSearchRequestDTO;
import com.example.inventory.enums.ProductField;
import com.example.inventory.enums.SearchOperator;
import com.example.inventory.enums.SortDirection;
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
public class SearchQueryBuilder {

    private static final List<String> KEYWORD_FIELDS = List.of(
            "title", "description", "sku", "brand", "category");

    public Query build(ProductSearchRequestDTO request) {
        Query query = new Query();
        if (request.getFilters() != null) {
            request.getFilters().forEach(filter -> query.addCriteria(toCriteria(filter)));
        }
        if (request.getSortBy() != null && !request.getSortBy().isBlank()) {
            ProductField field = searchFieldFor(request.getSortBy());
            Sort.Direction direction = request.getSortDirection() == SortDirection.DESC
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            query.with(Sort.by(direction, field.getValue()));
        }
        return query;
    }

    public Query buildKeywordQuery(String keyword) {
        String escapedKeyword = Pattern.quote(keyword);
        Criteria[] criteria = KEYWORD_FIELDS.stream()
                .map(field -> Criteria.where(field).regex(escapedKeyword, "i"))
                .toArray(Criteria[]::new);
        return new Query(new Criteria().orOperator(criteria));
    }

    public Query buildStandardQuery(String keyword, BigDecimal minPrice, BigDecimal maxPrice,
                                    BigDecimal minRating, BigDecimal maxRating,
                                    String category, String brand, String sortBy,
                                    SortDirection sortDirection) {
        Query query = new Query();
        List<Criteria> criteria = new java.util.ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            criteria.add(new Criteria().orOperator(KEYWORD_FIELDS.stream()
                    .map(field -> Criteria.where(field).regex(Pattern.quote(keyword.trim()), "i"))
                    .toArray(Criteria[]::new)));
        }
        if (minPrice != null || maxPrice != null) {
            Criteria priceCriteria = Criteria.where("price");
            if (minPrice != null) {
                priceCriteria.gte(minPrice);
            }
            if (maxPrice != null) {
                priceCriteria.lte(maxPrice);
            }
            criteria.add(priceCriteria);
        }
        if (minRating != null || maxRating != null) {
            Criteria ratingCriteria = Criteria.where("rating");
            if (minRating != null) {
                ratingCriteria.gte(minRating);
            }
            if (maxRating != null) {
                ratingCriteria.lte(maxRating);
            }
            criteria.add(ratingCriteria);
        }
        if (category != null && !category.isBlank()) {
            criteria.add(Criteria.where("category").is(category.trim()));
        }
        if (brand != null && !brand.isBlank()) {
            criteria.add(Criteria.where("brand").is(brand.trim()));
        }
        criteria.forEach(query::addCriteria);

        if (sortBy != null && !sortBy.isBlank()) {
            ProductField field = searchFieldFor(sortBy.trim());
            Sort.Direction direction = sortDirection == SortDirection.DESC
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            query.with(Sort.by(direction, field.getValue()));
        }
        return query;
    }

    private ProductField searchFieldFor(String value) {
        for (ProductField field : ProductField.values()) {
            if (isRestricted(field)) {
                continue;
            }
            if (field.getValue().equals(value) || field.name().equalsIgnoreCase(value)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Unsupported product search or sort field: " + value);
    }

    private Criteria toCriteria(ProductFilterDTO filter) {
        if (filter == null || filter.getField() == null || filter.getOperator() == null
                || filter.getValue() == null) {
            throw new IllegalArgumentException("Each filter requires field, operator and value");
        }
        ProductField field = filter.getField();
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

    private boolean isRestricted(ProductField field) {
        return field == ProductField.ID || field == ProductField.CREATED_AT || field == ProductField.UPDATED_AT;
    }

    private boolean isTextOperator(SearchOperator operator) {
        return operator == SearchOperator.CONTAINS || operator == SearchOperator.STARTS_WITH
                || operator == SearchOperator.ENDS_WITH || operator == SearchOperator.EQUALS;
    }

    private Class<?> fieldType(ProductField field) {
        return switch (field) {
            case PRICE, DISCOUNT_PERCENTAGE, RATING -> BigDecimal.class;
            default -> String.class;
        };
    }

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

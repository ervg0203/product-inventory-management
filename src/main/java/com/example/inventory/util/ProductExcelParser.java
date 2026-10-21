package com.example.inventory.util;

import com.example.inventory.dto.request.ProductRequestDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Converts the first worksheet of a product Excel file into request DTOs.
 */
@Component
public class ProductExcelParser {

    /**
     * Reads product rows from the uploaded workbook.
     *
     * @param file uploaded Excel workbook
     * @return parsed product requests
     */
    public List<ProductRequestDTO> parse(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Excel file cannot be empty");
        }
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() < 2) {
                throw new IllegalArgumentException(
                        "Excel file must contain a header and at least one product row");
            }

            Map<String, Integer> columns = readHeaders(sheet.getRow(sheet.getFirstRowNum()));
            DataFormatter formatter = new DataFormatter();
            List<ProductRequestDTO> products = new ArrayList<>();
            for (int rowIndex = sheet.getFirstRowNum() + 1;
                 rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isBlank(row, columns, formatter)) {
                    continue;
                }
                products.add(toProduct(row, columns, formatter));
            }
            if (products.isEmpty()) {
                throw new IllegalArgumentException("Excel file does not contain product rows");
            }
            return products;
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to read Excel product file", exception);
        } catch (RuntimeException exception) {
            if (exception instanceof IllegalArgumentException) {
                throw exception;
            }
            throw new IllegalArgumentException("Unable to read Excel product file", exception);
        }
    }

    private ProductRequestDTO toProduct(Row row, Map<String, Integer> columns,
                                        DataFormatter formatter) {
        return ProductRequestDTO.builder()
                .id(text(row, columns, "id", formatter))
                .sku(text(row, columns, "sku", formatter))
                .title(text(row, columns, "title", formatter))
                .description(text(row, columns, "description", formatter))
                .category(text(row, columns, "category", formatter))
                .price(decimal(row, columns, "price", formatter))
                .discountPercentage(decimal(row, columns, "discountpercentage", formatter))
                .rating(decimal(row, columns, "rating", formatter))
                .brand(text(row, columns, "brand", formatter))
                .returnPolicy(text(row, columns, "returnpolicy", formatter))
                .build();
    }

    private Map<String, Integer> readHeaders(Row row) {
        if (row == null) {
            throw new IllegalArgumentException("Excel file must contain a header row");
        }
        Map<String, Integer> columns = new HashMap<>();
        DataFormatter formatter = new DataFormatter();
        for (Cell cell : row) {
            String name = formatter.formatCellValue(cell)
                    .replaceAll("[ _-]", "")
                    .toLowerCase(Locale.ROOT);
            if (!name.isBlank()) {
                columns.put(name, cell.getColumnIndex());
            }
        }
        List<String> required = List.of("sku", "title", "description", "category", "price",
                "discountpercentage", "rating", "brand", "returnpolicy");
        if (!columns.keySet().containsAll(required)) {
            throw new IllegalArgumentException("Excel header must contain: " + required);
        }
        return columns;
    }

    private boolean isBlank(Row row, Map<String, Integer> columns, DataFormatter formatter) {
        return columns.values().stream().allMatch(index -> {
            Cell cell = row.getCell(index);
            return cell == null || formatter.formatCellValue(cell).isBlank();
        });
    }

    private String text(Row row, Map<String, Integer> columns, String name,
                        DataFormatter formatter) {
        Integer index = columns.get(name);
        if (index == null || row.getCell(index) == null) {
            return null;
        }
        String value = formatter.formatCellValue(row.getCell(index)).trim();
        return value.isEmpty() ? null : value;
    }

    private BigDecimal decimal(Row row, Map<String, Integer> columns, String name,
                               DataFormatter formatter) {
        String value = text(row, columns, name, formatter);
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid numeric value '" + value + "' in column " + name);
        }
    }
}

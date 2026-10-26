package com.example.inventory.util;

import com.example.inventory.dto.request.WarehouseRequestDTO;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Converts the first worksheet of a warehouse Excel file into request DTOs.
// * @param file input value
 * @return operation result
 */
@Component
public class WarehouseExcelParser {

    public List<WarehouseRequestDTO> parse(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Excel file cannot be empty");
        }
        try (InputStream input = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(input)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() < 2) {
                throw new IllegalArgumentException(
                        "Excel file must contain a header and at least one warehouse row");
            }
            Map<String, Integer> columns = readHeaders(sheet.getRow(sheet.getFirstRowNum()));
            DataFormatter formatter = new DataFormatter();
            List<WarehouseRequestDTO> warehouses = new ArrayList<>();
            for (int rowIndex = sheet.getFirstRowNum() + 1;
                 rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isBlank(row, columns, formatter)) {
                    continue;
                }
                warehouses.add(WarehouseRequestDTO.builder()
                        .id(text(row, columns, "id", formatter))
                        .code(text(row, columns, "code", formatter))
                        .name(text(row, columns, "name", formatter))
                        .address(text(row, columns, "address", formatter))
                        .city(text(row, columns, "city", formatter))
                        .state(text(row, columns, "state", formatter))
                        .postalCode(text(row, columns, "postalcode", formatter))
                        .active(booleanValue(row, columns, "active", formatter))
                        .build());
            }
            if (warehouses.isEmpty()) {
                throw new IllegalArgumentException("Excel file does not contain warehouse rows");
            }
            return warehouses;
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to read Excel warehouse file", exception);
        } catch (RuntimeException exception) {
            if (exception instanceof IllegalArgumentException) {
                throw exception;
            }
            throw new IllegalArgumentException("Unable to read Excel warehouse file", exception);
        }
    }
/**
 * Executes the read headers operation.
 *
 * @param row input value
 * @return operation result
 */

    private Map<String, Integer> readHeaders(Row row) {
        if (row == null) {
            throw new IllegalArgumentException("Excel file must contain a header row");
        }
        Map<String, Integer> columns = new HashMap<>();
        DataFormatter formatter = new DataFormatter();
        for (Cell cell : row) {
            String name = formatter.formatCellValue(cell).replaceAll("[ _-]", "")
                    .toLowerCase(Locale.ROOT);
            if (!name.isBlank()) {
                columns.put(name, cell.getColumnIndex());
            }
        }
        List<String> required = List.of("code", "name", "address", "city", "state", "postalcode");
        if (!columns.keySet().containsAll(required)) {
            throw new IllegalArgumentException("Excel header must contain: " + required);
        }
        return columns;
    }
/**
 * Executes the is blank operation.
 *
 * @param row input value
 * @param columns input value
 * @param formatter input value
 * @return operation result
// * @param String input value
 */

    private boolean isBlank(Row row, Map<String, Integer> columns, DataFormatter formatter) {
        return columns.values().stream().allMatch(index -> {
            Cell cell = row.getCell(index);
            return cell == null || formatter.formatCellValue(cell).isBlank();
        });
    }
/**
 * Executes the text operation.
 *
 * @param row input value
 * @param columns input value
 * @param name input value
 * @param formatter input value
 * @return operation result
// * @param String input value
 */

    private String text(Row row, Map<String, Integer> columns, String name,
                        DataFormatter formatter) {
        Integer index = columns.get(name);
        if (index == null || row.getCell(index) == null) {
            return null;
        }
        String value = formatter.formatCellValue(row.getCell(index)).trim();
        return value.isEmpty() ? null : value;
    }
/**
 * Executes the boolean value operation.
 *
 * @param row input value
 * @param columns input value
 * @param name input value
 * @param formatter input value
 * @return operation result
// * @param String input value
 */

    private boolean booleanValue(Row row, Map<String, Integer> columns, String name,
                                 DataFormatter formatter) {
        String value = text(row, columns, name, formatter);
        return value != null && Boolean.parseBoolean(value);
    }
}

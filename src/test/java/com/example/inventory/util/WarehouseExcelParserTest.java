package com.example.inventory.util;

import com.example.inventory.dto.request.WarehouseRequestDTO;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WarehouseExcelParserTest {
    private byte[] workbook(String[] headers, String[] values) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            var row = workbook.createSheet().createRow(0);
            for (int i = 0; i < headers.length; i++) row.createCell(i).setCellValue(headers[i]);
            row = workbook.getSheetAt(0).createRow(1);
            for (int i = 0; i < values.length; i++) row.createCell(i).setCellValue(values[i]);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            workbook.write(output);
            return output.toByteArray();
        }
    }

    @Test
    void parsesWarehouseWorkbookAndBoolean() throws Exception {
        byte[] bytes = workbook(new String[]{"Code", "Name", "Address", "City", "State", "Postal Code", "Active"},
                new String[]{"W1", "Central", "Main", "London", "LN", "E1", "true"});
        List<WarehouseRequestDTO> warehouses = new WarehouseExcelParser().parse(
                new MockMultipartFile("file", "warehouses.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes));
        assertEquals("W1", warehouses.get(0).getCode());
        assertTrue(warehouses.get(0).isActive());
    }

    @Test
    void rejectsEmptyFiles() {
        assertThrows(IllegalArgumentException.class,
                () -> new WarehouseExcelParser().parse(null));
    }
}

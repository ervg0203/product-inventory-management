package com.example.inventory.util;

import com.example.inventory.dto.request.ProductRequestDTO;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductExcelParserTest {
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
    void parsesProductWorkbookAndNormalizesHeaders() throws Exception {
        byte[] bytes = workbook(new String[]{"SKU", "Title", "Description", "Category", "Price",
                        "Discount Percentage", "Rating", "Brand", "Return-Policy", "ID"},
                new String[]{"S1", "Phone", "Desc", "Tech", "10.50", "5", "4.5", "Brand", "30 days", "id1"});
        List<ProductRequestDTO> products = new ProductExcelParser().parse(
                new MockMultipartFile("file", "products.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes));
        assertEquals(1, products.size());
        assertEquals("id1", products.get(0).getId());
        assertEquals(new BigDecimal("10.50"), products.get(0).getPrice());
    }

    @Test
    void rejectsEmptyMissingHeadersAndInvalidNumbers() throws Exception {
        ProductExcelParser parser = new ProductExcelParser();
        assertThrows(IllegalArgumentException.class, () -> parser.parse(null));
        byte[] invalid = workbook(new String[]{"sku", "title", "description", "category", "price",
                        "discountpercentage", "rating", "brand", "returnpolicy"},
                new String[]{"S1", "P", "D", "C", "not-number", "1", "1", "B", "R"});
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse(new MockMultipartFile("file", invalid)));
        byte[] missing = workbook(new String[]{"sku"}, new String[]{"S1"});
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse(new MockMultipartFile("file", missing)));
    }
}

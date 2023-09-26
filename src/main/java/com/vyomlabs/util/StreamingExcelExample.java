package com.vyomlabs.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class StreamingExcelExample {
    public static void main(String[] args) {
        String excelFilePath = Path.of("D:\\test").toAbsolutePath().toString()+"\\large_data.xlsx";
        int batchSize = 10000; // Adjust the batch size as needed
        int totalRows = 1048575; // Total number of rows you want to generate

        try {
            Workbook workbook = new SXSSFWorkbook(); // Use SXSSFWorkbook for streaming
            Sheet sheet = workbook.createSheet("Sheet1");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Column1");
            headerRow.createCell(1).setCellValue("Column2");
            headerRow.createCell(2).setCellValue("Column3");

            for (int rowCount = 1; rowCount <= totalRows; rowCount++) {
                // Generate data for each row (modify this part according to your data source)
                String data1 = "Value" + rowCount;
                String data2 = "AnotherValue" + rowCount;
                String data3 = "YetAnotherValue" + rowCount;

                Row row = sheet.createRow(rowCount);
                row.createCell(0).setCellValue(data1);
                row.createCell(1).setCellValue(data2);
                row.createCell(2).setCellValue(data3);

                // Flush the rows to the disk every batchSize rows
                if (rowCount % batchSize == 0) {
                    ((SXSSFSheet) sheet).flushRows(batchSize); // Flush the rows to disk
                }
            }

            // Save the Excel workbook
            try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
                workbook.write(fos);
                workbook.close(); // Close the workbook to release resources
            }

            System.out.println("Excel workbook created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

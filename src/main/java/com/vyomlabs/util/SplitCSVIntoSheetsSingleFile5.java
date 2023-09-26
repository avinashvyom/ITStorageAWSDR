package com.vyomlabs.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class SplitCSVIntoSheetsSingleFile5 {
    public static void main(String[] args) {
        int totalRows = 10000000; // Total number of rows you want to generate
        int batchSize = 1048576; // Max rows per sheet in Excel

        String excelFilePath = Path.of("D:\\test").toAbsolutePath().toString()+"\\large_data.xlsx";

        int sheetCount = (totalRows + batchSize - 1) / batchSize; // Calculate the number of sheets

        try {
            Workbook workbook = new SXSSFWorkbook(100); // Use SXSSFWorkbook for streaming
            
            System.out.println("Sheet count is : "+sheetCount);

            int rowCount = 0;

            for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
                Sheet sheet = workbook.createSheet("Sheet" + (sheetIndex + 1));
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Column1");
                headerRow.createCell(1).setCellValue("Column2");
                headerRow.createCell(2).setCellValue("Column3");

                int rowsInThisSheet = Math.min(batchSize, totalRows - rowCount);
                for (int i = 1; i < rowsInThisSheet; i++) {
                    // Generate data for each row (modify this part according to your data source)
                    String data1 = "Value" + rowCount;
                    String data2 = "AnotherValue" + rowCount;
                    String data3 = "YetAnotherValue" + rowCount;

                    Row row = sheet.createRow(i);
                    row.createCell(0).setCellValue(data1);
                    row.createCell(1).setCellValue(data2);
                    row.createCell(2).setCellValue(data3);

                    rowCount++;
                }
            }

            // Save the final Excel workbook
            try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
                workbook.write(fos);
                workbook.close();
            }

            System.out.println("Excel workbook created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

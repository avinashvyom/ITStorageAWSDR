package com.vyomlabs.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class SplitCSVIntoSheets {
    public static void main(String[] args) {
        // Define the CSV file path
        String csvFilePath = Path.of("D:\\test").toAbsolutePath().toString() + "\\" + "large_data.csv";

        // Define the number of rows per sheet
        int rowsPerSheet = 1048576; // Maximum rows per sheet in Excel

        // Initialize counters
        int rowCount = 0;
        int sheetCount = 1;

        try (CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(csvFilePath), CSVFormat.DEFAULT)) {
            Workbook workbook = new XSSFWorkbook(); // Create an Excel workbook

            while (rowCount < 10000000) { // Change this to the total number of rows you want
                // Create a new sheet for each batch of rows
                Sheet sheet = workbook.createSheet("Sheet" + sheetCount);
                sheetCount++;

                // Loop to generate and write rows to the CSV and Excel sheets
                for (int i = 0; i < rowsPerSheet; i++) {
                    // Generate data for each row (modify this part according to your data source)
                    String data1 = "Value" + rowCount;
                    String data2 = "AnotherValue" + rowCount;
                    String data3 = "YetAnotherValue" + rowCount;

                    // Write the data to the CSV file
                    csvPrinter.printRecord(data1, data2, data3);

                    // Write the data to the Excel sheet
                    Row row = sheet.createRow(i);
                    row.createCell(0).setCellValue(data1);
                    row.createCell(1).setCellValue(data2);
                    row.createCell(2).setCellValue(data3);

                    rowCount++;

                    if (rowCount >= 10000000) { // Change this to the total number of rows you want
                        break;
                    }
                }
            }

            // Save the Excel workbook to the same file
            try (FileOutputStream fos = new FileOutputStream(new File(csvFilePath + ".xlsx"))) {
                workbook.write(fos);
                System.out.println("CSV file split into Excel sheets successfully.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

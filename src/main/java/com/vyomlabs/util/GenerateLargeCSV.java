package com.vyomlabs.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class GenerateLargeCSV {
    public static void main(String[] args) {
        String csvFilePath = Path.of("D:\\test").toAbsolutePath().toString()+"\\large_data.csv";;
        int totalRows = 10000000; // Total number of rows you want to generate
        int batchSize = 10000; // Batch size for writing rows

        try (CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(csvFilePath), CSVFormat.DEFAULT)) {
            for (int rowCount = 0; rowCount < totalRows; rowCount++) {
                // Generate data for each row (modify this part according to your data source)
                String data1 = "Value " + rowCount;
                String data2 = "AnotherValue " + rowCount;
                String data3 = "YetAnotherValue " + rowCount;

                // Write the data to the CSV file
                csvPrinter.printRecord(data1, data2, data3);

                // Flush the CSVPrinter periodically to avoid excessive memory usage
                if (rowCount % batchSize == 0) {
                    csvPrinter.flush();
                }
            }

            System.out.println("CSV file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

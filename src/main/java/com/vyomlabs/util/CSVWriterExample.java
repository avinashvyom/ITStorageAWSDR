package com.vyomlabs.util;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class CSVWriterExample {
    public static void main(String[] args) {
        // Define the file path where you want to save the CSV file
        String filePath = Path.of("D:\\test").toAbsolutePath().toString() + "\\" + "large_data.csv";

        // Define the CSV format
        @SuppressWarnings("deprecation")
		CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader("Column1", "Column2", "Column3"); // Customize headers as needed

        try (CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(filePath), csvFormat)) {
            // Loop to generate and write more than 1 million rows
            for (int i = 1; i <= 10000000; i++) {
                // Generate data for each row (modify this part according to your data source)
                String data1 = "Value" + i;
                String data2 = "AnotherValue" + i;
                String data3 = "YetAnotherValue" + i;

                // Write the data to the CSV file
                csvPrinter.printRecord(data1, data2, data3);
            }

            System.out.println("CSV file with more than 1 million rows created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

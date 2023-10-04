package com.vyomlabs.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class FileGenerator {

	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		for (int i = 1; i <= 20; i++) {
			File file = new File(Path.of("D:\\test").toAbsolutePath().toString() + "\\" + i + ".txt");
			file.createNewFile();
			
			if (i % 2 == 0) {
				FileWriter fw = new FileWriter(file);
				fw.write("Welcome to File " + i + "\n");
				fw.close();
			}

			// fw.flush();
//			Thread.sleep(3000);
			// System.out.println("Wrote data........");
			
		}
		System.out.println("done.....");
		// fw.close();
	}
}

//public class FileGenerator {
//    public static void main(String[] args) {
//        // Specify the path to the file
//        String filePath = Path.of("D:\\test").toAbsolutePath().toString() + "\\" /* + i + "." */ + "File 13.txt";
//
//        try {
//            // Loop to create, write, and overwrite the file 10 times
//            for (int i = 1; i <= 10; i++) {
//                // Create a FileWriter with the file path (false for overwriting)
//                FileWriter fileWriter = new FileWriter(filePath, false);
//
//                // Write the content to the file
//                String content = "This is content iteration " + i + "\n";
//                fileWriter.write(content);
//
//                // Close the FileWriter to flush and release resources
//                fileWriter.close();
//
//                System.out.println("Content written for iteration " + i);
//
//                // Sleep for a moment (optional) to simulate updates
//                Thread.sleep(3000);
//            }
//
//            System.out.println("File writing and overwriting completed.");
//        } catch (IOException | InterruptedException e) {
//            System.err.println("An error occurred: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//}

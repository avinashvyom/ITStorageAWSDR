package com.vyomlabs.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class FileGenerator {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		for (int i = 1; i <= 10; i++) {
			File file = new File(Path.of("D:\\Central Data").toAbsolutePath().toString() + "\\" + "File 8_" + i + ".txt");
			file.createNewFile();
			FileWriter fw = new FileWriter(file,true);
			fw.write("Welcome to File "+i);
			fw.close();		
		}
		System.out.println("done................");
	}
}

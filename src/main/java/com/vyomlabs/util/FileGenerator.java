package com.vyomlabs.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

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
//		String email = "s,d,ft,eee,frfdf,sdfdcfefv,dfsgdvsrger";
//		String[] split = email.split(",");
//		System.out.println(Arrays.deepToString(split));
//		System.out.println(split[0]);
//		System.out.println(file.canWrite());
//		System.out.println(file.canRead());
//		System.out.println(file.canExecute());
		// FileUtils.touch(file);

//		try {
//			File file = new File(Path.of("").toAbsolutePath().toString() + "\\" + "File_temp" + ".txt");
//			// Open the file in read-only mode
//			System.out.println(file.getName());
//			System.out.println(file.getAbsolutePath());
//			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
//			Desktop desktop = Desktop.getDesktop();
//			desktop.open(file);
//			
//
//			// Get the file channel
//			FileChannel fileChannel = randomAccessFile.getChannel();
//
//			// Try to acquire a lock on the file
//			FileLock fileLock = fileChannel.lock();
//			System.out.println(fileLock.toString());
//
//			if (fileLock != null) {
//				// Lock acquisition was successful, file is not open
//				System.out.println("File is not open.");
//
//				// Release the lock
//				// fileLock.release();
//			} else {
//				// Lock acquisition failed, file is already open
//				System.out.println("File is already open.");
//			}
//
//			// Close the file
//			randomAccessFile.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	// file.i
	// file.

}

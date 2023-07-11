package com.vyomlabs.util;

import java.text.DecimalFormat;

public class FileSizeCalculator {

	public static String getFileSize(long size) {
		DecimalFormat df = new DecimalFormat("0.00");
		float sizeKb = 1024.0f;
		float sizeMb = sizeKb * sizeKb;
		float sizeGb = sizeMb * sizeKb;
		float sizeTerra = sizeGb * sizeKb;

		if (size < sizeKb)
			return df.format(size) + " B";
		else if (size < sizeMb)
			return df.format(size / sizeKb) + " KB";
		else if (size < sizeGb)
			return df.format(size / sizeMb) + " MB";
		else if (size < sizeTerra)
			return df.format(size / sizeGb) + " GB";
		else
			return df.format(size);
	}

//	public static void main(String[] args) {
//		System.out.println("Welcome to file size : ");
//		try {
//			System.out.println(getFileSize(
//					Files.size(Path.of("D:\\Vyom Projects\\DR setup AWS S3\\Final jar\\application.properties"))));
//			System.out.println(getFileSize(
//					Files.size(Path.of("D:\\Vyom Projects\\DR setup AWS S3\\Final jar\\jdk-17_windows-x64_bin.exe"))));
//			System.out.println(getFileSize(
//					Files.size(Path.of("D:\\Vyom Projects\\DR setup AWS S3\\Final jar\\s3-backup-service.jar"))));
//			System.out
//					.println(getFileSize(Files.size(Path.of("D:\\Central data backup\\Finance\\user 2\\user 2.txt"))));
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

}

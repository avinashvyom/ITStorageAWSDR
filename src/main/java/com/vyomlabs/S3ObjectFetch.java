package com.vyomlabs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class S3ObjectFetch {

	private String USAGE_REPORT_FILE_NAME = "Usage_Report_" + getCurrentMonthAndYear() + ".csv";
	private String COST_REPORT_FILE_NAME = "Cost_Report_" + getCurrentMonthAndYear() + ".csv";

	public File getCostReport(S3Client s3Client, String BUCKET_NAME) throws IOException {
		// TODO Auto-generated method stub
		String path = Path.of("").toAbsolutePath().toString() + "\\" + COST_REPORT_FILE_NAME;

		GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(BUCKET_NAME)
				.key(COST_REPORT_FILE_NAME).build();
		ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(getObjectRequest);
		FileOutputStream outputStream = new FileOutputStream(path);

		int copy = IOUtils.copy(inputStream, outputStream);
		System.out.println("no of bytes copied : " + copy);

		inputStream.close();
		outputStream.close();

		return new File(Path.of("").toAbsolutePath().toString() + "\\" + COST_REPORT_FILE_NAME);
	}

	public File getUsageReport(S3Client s3Client, String BUCKET_NAME) throws IOException {
		String path = Path.of("").toAbsolutePath().toString() + "\\" + USAGE_REPORT_FILE_NAME;

		GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(BUCKET_NAME)
				.key(USAGE_REPORT_FILE_NAME).build();
		ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(getObjectRequest);
		FileOutputStream outputStream = new FileOutputStream(path);

//		byte[] buffer = new byte[4096];
//		int bytesRead = -1;
//		while ((bytesRead = inputStream.read(buffer)) != -1) {
//			outputStream.write(buffer, 0, bytesRead);
//		}
		int copy = IOUtils.copy(inputStream, outputStream);
		System.out.println("no of bytes copied : " + copy);
		inputStream.close();
		outputStream.close();

		return new File(Path.of("").toAbsolutePath().toString() + "\\" + USAGE_REPORT_FILE_NAME);
	}

	private String getCurrentMonthAndYear() {
		return new SimpleDateFormat("MMM-YYYY").format(new Date());
	}
	
//	public static void main(String[] args) {
//		S3ObjectFetch s3ObjectFetch = new S3ObjectFetch();
//		System.out.println("Current month is : "+s3ObjectFetch.getCurrentMonthAndYear());
//	}
}

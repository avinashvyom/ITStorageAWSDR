package com.vyomlabs;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

public class S3ObjectFetch {

	private String USAGE_REPORT_FILE_NAME = "Usage_Report_" + getCurrentMonthAndYear() + ".csv";
	private String COST_REPORT_FILE_NAME = "Cost_Report_" + getCurrentMonthAndYear() + ".csv";

	public File getCostReport(AmazonS3 s3Client, String BUCKET_NAME) {
		try {
			String path = Path.of("").toAbsolutePath().toString() + "\\" + COST_REPORT_FILE_NAME;

			GetObjectRequest getObjectRequest = new GetObjectRequest(BUCKET_NAME, COST_REPORT_FILE_NAME);

			S3Object s3Object = s3Client.getObject(getObjectRequest);
			S3ObjectInputStream inputStream = s3Object.getObjectContent();
			FileOutputStream outputStream = new FileOutputStream(path);

			IOUtils.copy(inputStream, outputStream);

			inputStream.close();
			outputStream.close();

			return new File(Path.of("").toAbsolutePath().toString() + "\\" + COST_REPORT_FILE_NAME);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public File getUsageReport(AmazonS3 s3Client, String BUCKET_NAME) {
		try {
			String path = Path.of("").toAbsolutePath().toString() + "\\" + USAGE_REPORT_FILE_NAME;

			GetObjectRequest getObjectRequest = new GetObjectRequest(BUCKET_NAME, USAGE_REPORT_FILE_NAME);
			S3Object s3Object = s3Client.getObject(getObjectRequest);
			S3ObjectInputStream inputStream = s3Object.getObjectContent();
			FileOutputStream outputStream = new FileOutputStream(path);

			IOUtils.copy(inputStream, outputStream);
			inputStream.close();
			outputStream.close();

			return new File(Path.of("").toAbsolutePath().toString() + "\\" + USAGE_REPORT_FILE_NAME);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
	}

	private String getCurrentMonthAndYear() {
		return new SimpleDateFormat("MMM-YYYY").format(new Date());
	}

}

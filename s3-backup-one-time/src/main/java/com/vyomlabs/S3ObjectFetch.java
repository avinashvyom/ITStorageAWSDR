package com.vyomlabs;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

public class S3ObjectFetch {

	private final static Logger logger = Logger.getLogger(S3ObjectFetch.class);
	
	public File getReportFromBucket(AmazonS3 s3Client, String BUCKET_NAME, String REPORT_NAME) {
		try {
			String path = Path.of("").toAbsolutePath().toString() + "\\" + REPORT_NAME;

			logger.info("report file Name : "+REPORT_NAME);

			GetObjectRequest getObjectRequest = new GetObjectRequest(BUCKET_NAME, REPORT_NAME);

			S3Object s3Object = s3Client.getObject(getObjectRequest);
			S3ObjectInputStream inputStream = s3Object.getObjectContent();
			FileOutputStream outputStream = new FileOutputStream(path);

			IOUtils.copy(inputStream, outputStream);

			inputStream.close();
			outputStream.close();

			return new File(Path.of("").toAbsolutePath().toString() + "\\" + REPORT_NAME);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}

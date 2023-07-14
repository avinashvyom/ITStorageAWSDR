package com.vyomlabs.configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.vyomlabs.util.PropertiesExtractor;
import com.vyomlabs.util.TextEncryptorAndDecryptor;

public class AppConfig {
	static PropertiesExtractor propertiesExtractor = new PropertiesExtractor();

	private final static String accessKey = TextEncryptorAndDecryptor
			.decrypt(propertiesExtractor.getProperty("s3.access-key"));
	private final static String secretKey = TextEncryptorAndDecryptor
			.decrypt(propertiesExtractor.getProperty("s3.secret-key"));

	private static String region = propertiesExtractor.getProperty("s3.region");

	public static AmazonS3 getS3Client() {
		return AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
				.withRegion(region).build();
	}

}

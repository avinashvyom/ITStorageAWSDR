package com.vyomlabs;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vyomlabs.entity.LambdaFunctionOutput;
import com.vyomlabs.util.PropertiesExtractor;
import com.vyomlabs.util.TextEncryptorAndDecryptor;

public class S3LambdaTrigger {

	private final Logger logger = Logger.getLogger(S3LambdaTrigger.class);
	Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public int triggerLambdaForReportGeneration() throws IOException {
		PropertyConfigurator.configure("D:\\Vyom Projects\\DR setup AWS S3\\FileBackupToAWSS3\\src\\main\\resources\\log4j.properties");
		logger.info("Lambda Trigger started..........");
		PropertiesExtractor propertiesExtractor = new PropertiesExtractor();
		String accessKey = TextEncryptorAndDecryptor.decrypt(propertiesExtractor.getProperty("s3.access-key"));
		String secretKey = TextEncryptorAndDecryptor.decrypt(propertiesExtractor.getProperty("s3.secret-key"));
		String lambdaFunctionName = "Monthly-Report-Generation-Function";
		logger.info("Lambda function name : " + lambdaFunctionName);
		String event = "{\"key1\": \"value1\",  \"key2\": \"value2\",  \"key3\": \"value3\"}";
		AWSLambdaClientBuilder lambdaClientBuilder = AWSLambdaClientBuilder.standard()
				.withRegion(propertiesExtractor.getProperty("s3.region"))
				.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)));

		AWSLambda awsLambda = lambdaClientBuilder.build();

		InvokeRequest request = new InvokeRequest().withFunctionName(lambdaFunctionName).withPayload(event);
		logger.info("Lambda invoked...........");
		InvokeResult result = awsLambda.invoke(request);
		String lambdaFunctionOutput = new String(result.getPayload().array());
		logger.info("Lambda function output : " + lambdaFunctionOutput);
		LambdaFunctionOutput lambdaOutput = gson.fromJson(lambdaFunctionOutput, LambdaFunctionOutput.class);
		return lambdaOutput.getStatusCode();
	}

}

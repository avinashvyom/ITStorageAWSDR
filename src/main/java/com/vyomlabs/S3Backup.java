package com.vyomlabs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.apache.log4j.Logger;

import com.opencsv.exceptions.CsvValidationException;
import com.vyomlabs.emailservice.EmailService;
import com.vyomlabs.filebackupdata.FileBackupDetails;
import com.vyomlabs.filebackupdata.FileStatus;
import com.vyomlabs.filebackupdata.FileUploadCategory;
import com.vyomlabs.filebackupdata.FileUploadDetailsService;
import com.vyomlabs.filebackupdata.FileUploadStatus;
import com.vyomlabs.util.FileSizeCalculator;
import com.vyomlabs.util.PropertiesExtractor;
import com.vyomlabs.util.TextEncryptorAndDecryptor;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3Backup {

	private final static Logger logger = Logger.getLogger(S3Backup.class);
	public static FileUploadCategory fileUploadCategory;
//	 BasicConfigurator.configure();
//	 BasicConfigurator basicConfigurator = new BasicConfigurator();
	// Replace with your AWS access key ID and secret access key
	// static String accessKeyId = "AKIA46KGYVBTDKOMTCXQ";
	// static String secretAccessKey = "QZil7QUbHLctXgSISkTJK3U9gRR3NzaMn20G8l5b";
	private static FileStatus fileStatus;
	// S3Config s3Config = new S3Config();
	static S3LambdaTrigger s3LambdaTrigger = new S3LambdaTrigger();
	// private static final String REGION = "ap-south-1"; // Change to your desired
	// AWS region

	public static void main(String[] args) throws IOException, CsvValidationException {

		PropertiesExtractor propertiesExtractor = new PropertiesExtractor();
		Region region = Region.of(propertiesExtractor.getProperty("s3.region"));
		String BUCKET_NAME = propertiesExtractor.getProperty("s3.bucket-name");
		S3Client s3Client = S3Client.builder().region(region)
				.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
						TextEncryptorAndDecryptor.decrypt(propertiesExtractor.getProperty("s3.access-key")),
						TextEncryptorAndDecryptor.decrypt(propertiesExtractor.getProperty("s3.secret-key")))))
				.build();
		if (FileUploadDetailsService.checkIfBackupDetailsFileExists()
				&& !FileUploadDetailsService.getFailureFileDetails().isEmpty()) {
			fileUploadCategory = FileUploadCategory.FAILURE_FILES_UPLOAD;
			logger.info("Failure files uploading.......");
			FileUploadDetailsService.getFailureFileDetails().forEach(file -> {
				try {
					uploadFileToS3(s3Client, BUCKET_NAME, file.getFilePathInS3(),
							Path.of(file.getFilePathOnLocalDrive()),file.getFileStatus());
					sendNotificationEmail(FileUploadDetailsService.getFailureFileDetails(), s3Client, BUCKET_NAME);
				} catch (IOException | CsvValidationException e) {
					// TODO Auto-generated catch block
					logger.info("Exception caught in line no 53.....................");
					e.printStackTrace();
				}
			});
		} else {
			fileUploadCategory = FileUploadCategory.DIFFERENTIAL_FILES_UPLOAD;
			try {
				logger.info("Fresh differential files uploading.......");
				// "D:/Central Data"
				String backupFolderPath = propertiesExtractor.getProperty("s3upload.input-folder-path");
				logger.info("backupFolderPath :" + backupFolderPath);
				File backupFolder = new File(backupFolderPath); // Specify the folder to backup
				// Iterate over the files and folders in the backup folder
//				List<Path> list = Files.walk(backupFolder.toPath()).filter(path -> !Files.isDirectory(path))
//				.filter(path -> isRecentlyUpdated(path)).toList();
//				for(Path p : list) {
//					p.toFile().
//				}
				Files.walk(backupFolder.toPath()).filter(path -> !Files.isDirectory(path))
						.filter(path -> isRecentlyUpdated(path)).forEach(path -> {
							String key = backupFolder.toPath().relativize(path).toString();
							key = backupFolderPath.substring(3) + "/" + key;
							key = key.replace("\\", "/");
							logger.info("key: " + key);
							logger.info("path: " + path);
							try {
								uploadFileToS3(s3Client, BUCKET_NAME, key, path);
							} catch (IOException e) {
								logger.info("Exception in reading files......");
								e.printStackTrace();
							}

						});

				logger.info("Backup completed successfully.........................");
				FileUploadDetailsService.writeDataInCSVFile();
				sendNotificationEmail(FileUploadDetailsService.getFileDetails(), s3Client, BUCKET_NAME);

			} catch (Exception e) {
				System.err.println("Error occurred during backup: " + e.getMessage());
				sendNotificationEmail(FileUploadDetailsService.getFileDetails(), s3Client, BUCKET_NAME);
			} finally {
				s3Client.close();
			}
		}
	}

	private static void uploadFileToS3(S3Client s3Client, String bucketName, String key, Path filePath,
			String... fileStatuses) throws IOException {
		long size = Files.size(filePath);
		try {
			PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(key)
					.build();/*
								 * .storageClass( StorageClass. STANDARD_IA)
								 */
			s3Client.putObject(request, filePath);
			logger.info("Uploaded file: " + filePath);
			if (fileStatuses.length > 0) {
				//System.out.println("Types of files Uploading : "+fileUploadCategory);
				FileUploadDetailsService.backupFileData(key, filePath, FileUploadStatus.SUCCESS, fileStatuses[0],
						FileSizeCalculator.getFileSize(size));
			} else {
				//System.out.println("Types of files Uploading : "+fileUploadCategory);
				FileUploadDetailsService.backupFileData(key, filePath, FileUploadStatus.SUCCESS, fileStatus.name(),
						FileSizeCalculator.getFileSize(size));
			}
		} catch (Exception e) {
			System.err.println("Error uploading file in catch block: " + filePath + " - " + e.getMessage());
			if (fileStatuses.length > 0) {
				//System.out.println("Types of files Uploading : "+fileUploadCategory);
				FileUploadDetailsService.backupFileData(key, filePath, FileUploadStatus.FAILED, fileStatuses[0],
						FileSizeCalculator.getFileSize(size));
			} else {
				//System.out.println("Types of files Uploading in catch block: "+fileUploadCategory);
				FileUploadDetailsService.backupFileData(key, filePath, FileUploadStatus.FAILED, fileStatus.name(),
						FileSizeCalculator.getFileSize(size));
			}
		}

	}

	private static boolean isRecentlyUpdated(Path filePath) {
		try {
			BasicFileAttributes fileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);
			Instant lastModified = fileAttributes.lastModifiedTime().toInstant();
			Instant lastCreated = fileAttributes.creationTime().toInstant();
			if (lastModified.compareTo(lastCreated) == 0) {
				fileStatus = FileStatus.CREATED;
				logger.info("File Status for " + filePath.toFile().getName() + " is :" + fileStatus);
			} else {
				fileStatus = FileStatus.MODIFIED;
				logger.info("File Status for " + filePath.toFile().getName() + " is :" + fileStatus);
			}
			Instant oneMonthAgo = Instant.now().minus(30, ChronoUnit.DAYS);
			return lastModified.isAfter(oneMonthAgo);

		} catch (Exception e) {
			System.err.println("Error retrieving file attributes: " + filePath + " - " + e.getMessage());
			return false;
		}
	}

	private static void sendNotificationEmail(List<FileBackupDetails> fileList, S3Client s3Client, String BUCKET_NAME)
			throws IOException {
		File costReport;
		File usageReport;
		EmailService emailService = new EmailService();
		emailService.setFileList(fileList);
		logger.info("Trigger to lambda function....................");
		if (s3LambdaTrigger.triggerLambdaForReportGeneration() == 200) {
			S3ObjectFetch s3ObjectFetch = new S3ObjectFetch();
			logger.info("Fetching cost report file....................");
			costReport = s3ObjectFetch.getCostReport(s3Client, BUCKET_NAME);
			logger.info("Fetching usage report file....................");
			usageReport = s3ObjectFetch.getUsageReport(s3Client, BUCKET_NAME);
			emailService.sendMail(costReport, usageReport);
		}
	}
}

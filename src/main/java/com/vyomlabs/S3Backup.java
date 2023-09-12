package com.vyomlabs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.opencsv.exceptions.CsvValidationException;
import com.vyomlabs.configuration.AppConfig;
import com.vyomlabs.emailservice.EmailService;
import com.vyomlabs.entity.FileBackupDetails;
import com.vyomlabs.entity.FileStatus;
import com.vyomlabs.entity.FileUploadCategory;
import com.vyomlabs.entity.FileUploadStatus;
import com.vyomlabs.filebackupdata.FileUploadDetailsService;
import com.vyomlabs.util.FileSizeCalculator;
import com.vyomlabs.util.PropertiesExtractor;

public class S3Backup {

	private final static Logger logger = Logger.getLogger(S3Backup.class);

	public static FileUploadCategory fileUploadCategory;

	private static FileStatus fileStatus;
	private static File csvFile;

	static S3LambdaTrigger s3LambdaTrigger = new S3LambdaTrigger();
	static PropertiesExtractor propertiesExtractor = new PropertiesExtractor();

	private final static long FILE_SIZE_IN_BYTES = 5368709120l;

	public static void main(String[] args) throws IOException, CsvValidationException {
		PropertyConfigurator.configure(Paths.get("").toAbsolutePath().toString() + "\\" + "application.properties");
		String BUCKET_NAME = propertiesExtractor.getProperty("s3.bucket-name");
		AmazonS3 s3Client = AppConfig.getS3Client();

		// String backupFolderPath =
		// propertiesExtractor.getProperty("s3upload.input-folder-path");
		// Stream<Path> filter =
		// Files.walk(Path.of(propertiesExtractor.getProperty("s3upload.input-folder-path")))
		// .filter(path -> !Files.isDirectory(path)).filter(path ->
		// isRecentlyUpdated(path));

		if (FileUploadDetailsService.checkIfBackupDetailsFileExists()
				&& !FileUploadDetailsService.getFailureFileDetails().isEmpty()) {
			csvFile = FileUploadDetailsService.getCSVFile();
			fileUploadCategory = FileUploadCategory.FAILURE_FILES_UPLOAD;
			logger.info("Failure files uploading.......");
			FileUploadDetailsService.getFailureFileDetails().forEach(file -> {
				try {
					// logger.info("File size : " +
					// Path.of(file.getFilePathOnLocalDrive().toString()).toFile().length());
					if (Path.of(file.getFilePathOnLocalDrive().toString()).toFile().length() > FILE_SIZE_IN_BYTES) {
						initiateMultipartUpload(s3Client, BUCKET_NAME, Path.of(file.getFilePathOnLocalDrive()),
								file.getFilePathInS3());
					} else {
						uploadFileToS3(s3Client, BUCKET_NAME, file.getFilePathInS3(),
								Path.of(file.getFilePathOnLocalDrive()), file.getFileStatus());
					}
				} catch (IOException e) {
					logger.info("Exception caught in line no 53.....................");
					e.printStackTrace();
				}
			});
			sendNotificationEmail(FileUploadDetailsService.getFailureFileDetails(), s3Client, BUCKET_NAME, csvFile);
		} else {
			fileUploadCategory = FileUploadCategory.DIFFERENTIAL_FILES_UPLOAD;
			csvFile = FileUploadDetailsService.createCSVFile();
			try {
				logger.info("Fresh differential files uploading.......");
				String backupFolderPath = propertiesExtractor.getProperty("s3upload.input-folder-path");
				logger.info("backupFolderPath :" + backupFolderPath);
				File backupFolder = new File(backupFolderPath);
				Files.walk(backupFolder.toPath()).filter(path -> !Files.isDirectory(path))
						.filter(path -> isRecentlyUpdated(path)).forEach(path -> {
							String key = backupFolder.toPath().relativize(path).toString();
							key = backupFolderPath.substring(3) + "/" + key;
							key = key.replace("\\", "/");
							logger.info("key: " + key);
							logger.info("path: " + path);
							try {
								System.out.println("File size : " + path.toFile().length());
								if (path.toFile().length() > FILE_SIZE_IN_BYTES) {
									logger.info("Initiating multipart upload...............");
									initiateMultipartUpload(s3Client, BUCKET_NAME, path, key);
								} else {
									uploadFileToS3(s3Client, BUCKET_NAME, key, path);
								}
							} catch (IOException e) {
								logger.info("Exception in reading files......");
								e.printStackTrace();
							}
						});
				logger.info("Backup completed successfully.........................");
				// FileUploadDetailsService.writeDataInCSVFile();
				sendNotificationEmail(FileUploadDetailsService.getFileDetails(), s3Client, BUCKET_NAME, csvFile);
			} catch (Exception e) {
				logger.error("Error occurred during backup: " + e.getMessage());
				e.printStackTrace();
				sendNotificationEmail(FileUploadDetailsService.getFileDetails(), s3Client, BUCKET_NAME, csvFile);
			} finally {
			}
		}
	}

	private static void initiateMultipartUpload(AmazonS3 s3Client, String BUCKET_NAME, Path filePath, String key)
			throws IOException {
		TransferManager transferManager = null;
		long size = Files.size(filePath);
		try {
			transferManager = TransferManagerBuilder.standard().withS3Client(s3Client)
					.withMultipartUploadThreshold((long) (104857600)).withMinimumUploadPartSize((long) (104857600))
					.build();
			PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, key, filePath.toFile());
			request.withStorageClass(StorageClass.StandardInfrequentAccess);

			Upload upload = transferManager.upload(request);
			logger.info("Multipart upload started for file : " + filePath.getFileName());
			upload.waitForCompletion();
			logger.info("Multipart upload completed for file : " + filePath.getFileName());
			if (fileUploadCategory.equals(FileUploadCategory.DIFFERENTIAL_FILES_UPLOAD)) {
				csvFile = FileUploadDetailsService.backupFileData(key, filePath, FileUploadStatus.SUCCESS,
						fileStatus.name(), FileSizeCalculator.getFileSize(size), csvFile);
			}
		} catch (Exception e) {
			if (fileUploadCategory.equals(FileUploadCategory.DIFFERENTIAL_FILES_UPLOAD)) {
				csvFile = FileUploadDetailsService.backupFileData(key, filePath, FileUploadStatus.FAILED,
						fileStatus.name(), FileSizeCalculator.getFileSize(size), csvFile);
			}
			e.printStackTrace();
			logger.error("Error occured during Multipart upload....." + e.getMessage());

		}
	}

	private static void uploadFileToS3(AmazonS3 s3Client, String bucketName, String key, Path filePath,
			String... fileStatuses) throws IOException {
		logger.info(filePath.toString());
		long size = Files.size(filePath);
		try {
			PutObjectRequest request = new PutObjectRequest(bucketName, key, filePath.toFile());
			request.withStorageClass(StorageClass.StandardInfrequentAccess);
			s3Client.putObject(request);
			logger.info("Uploaded file: " + filePath);
			if (fileStatuses.length > 0) {
				if (fileUploadCategory.equals(FileUploadCategory.DIFFERENTIAL_FILES_UPLOAD)) {
					csvFile = FileUploadDetailsService.backupFileData(key, filePath, FileUploadStatus.SUCCESS,
							fileStatuses[0], FileSizeCalculator.getFileSize(size), csvFile);
				}
			} else {
				if (fileUploadCategory.equals(FileUploadCategory.DIFFERENTIAL_FILES_UPLOAD)) {
					csvFile = FileUploadDetailsService.backupFileData(key, filePath, FileUploadStatus.SUCCESS,
							fileStatus.name(), FileSizeCalculator.getFileSize(size), csvFile);
				}
			}
		} catch (Exception e) {
			logger.error("Error uploading file : " + filePath + " - " + e.getMessage());
			e.printStackTrace();
			if (fileStatuses.length > 0) {
				if (fileUploadCategory.equals(FileUploadCategory.DIFFERENTIAL_FILES_UPLOAD)) {
					csvFile = FileUploadDetailsService.backupFileData(key, filePath, FileUploadStatus.FAILED,
							fileStatuses[0], FileSizeCalculator.getFileSize(size), csvFile);
				}
			} else {
				if (fileUploadCategory.equals(FileUploadCategory.DIFFERENTIAL_FILES_UPLOAD)) {
					csvFile = FileUploadDetailsService.backupFileData(key, filePath, FileUploadStatus.FAILED,
							fileStatus.name(), FileSizeCalculator.getFileSize(size), csvFile);
				}
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
			int fileUploadDuration = Integer.parseInt(propertiesExtractor.getProperty("files.upload.duration"));
			Instant oneMonthAgo = Instant.now().minus(fileUploadDuration, ChronoUnit.DAYS);
			return lastModified.isAfter(oneMonthAgo);
		} catch (Exception e) {
			logger.error("Error retrieving file attributes: " + filePath + " - " + e.getMessage());
			return false;
		}
	}

	private static void sendNotificationEmail(List<FileBackupDetails> fileList, AmazonS3 s3Client, String BUCKET_NAME,
			File csvFile) throws IOException {
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
			emailService.sendMail(costReport, usageReport, csvFile);
		}
	}
}

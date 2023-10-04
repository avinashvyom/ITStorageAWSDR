package com.vyomlabs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.vyomlabs.entity.FileDetails;
import com.vyomlabs.entity.FileUploadCategory;
import com.vyomlabs.entity.FileUploadStatus;
import com.vyomlabs.filebackupdata.FileUploadDetailsService;
import com.vyomlabs.util.FileDataProvider;
import com.vyomlabs.util.FileSizeCalculator;
import com.vyomlabs.util.PropertiesExtractor;

public class S3Backup {

	private final static Logger logger = Logger.getLogger(S3Backup.class);

	public static FileUploadCategory fileUploadCategory;

	//private static FileStatus fileStatus;

	private static File csvFile;

	private static String USAGE_REPORT_FILE_NAME = "Usage_Report_" + getCurrentMonthAndYear() + ".csv";

	private static String COST_REPORT_FILE_NAME = "Cost_Report_" + getCurrentMonthAndYear() + ".csv";

	static S3LambdaTrigger s3LambdaTrigger = new S3LambdaTrigger();

	static PropertiesExtractor propertiesExtractor = new PropertiesExtractor();

	private final static long FILE_SIZE_IN_BYTES = 5368709120l;

	public static void main(String[] args) throws IOException, CsvValidationException {

		PropertyConfigurator.configure(Paths.get("").toAbsolutePath().toString() + "\\" + "application.properties");

		String BUCKET_NAME = propertiesExtractor.getProperty("s3.bucket-name");

		AmazonS3 s3Client = AppConfig.getS3Client();

		// failureFileDetails.addAll(addThePendingFiles());

		if (FileUploadDetailsService.checkIfBackupDetailsFileExists()) {

			// If failure files are getting uploaded, create new csv file as
			// MONTHNAME_YEAR_RERUN.csv and upload the files
			// and create records in RERUN csv file.
			fileUploadCategory = FileUploadCategory.FAILURE_FILES_UPLOAD;
			FileUploadDetailsService.createCSVFile(fileUploadCategory);
			csvFile = FileUploadDetailsService.getMainCSVFile();
			List<FileDetails> failureFileDetails = FileUploadDetailsService.getFailureFileDetails(csvFile);
			if (!failureFileDetails.isEmpty()) {
				logger.info("Failure files uploading.......");
				failureFileDetails.forEach(file -> {
					try {
						if (Path.of(file.getFilePathOnLocalDrive().toString()).toFile().length() > FILE_SIZE_IN_BYTES) {
							initiateMultipartUpload(s3Client, BUCKET_NAME, Path.of(file.getFilePathOnLocalDrive()),
									file.getFilePathInS3(),file.getFileStatus());
						} else {
							uploadFileToS3(s3Client, BUCKET_NAME, file.getFilePathInS3(),
									Path.of(file.getFilePathOnLocalDrive()),file.getFileStatus());
						}
					} catch (IOException e) {
						logger.info("Exception caught in line no 53.....................");
						e.printStackTrace();
					}
				});
				sendNotificationEmail(FileUploadDetailsService.getFileDetails(), s3Client, BUCKET_NAME, csvFile);
			} else {
				logger.info("All files are successfully uploaded.................");
				System.exit(0);
			}
		} else {
			fileUploadCategory = FileUploadCategory.FRESH_DIFFERENTIAL_FILES_UPLOAD;
			csvFile = FileUploadDetailsService.createCSVFile(fileUploadCategory);
			try {
				logger.info("Fresh differential files uploading.......");
				String backupFolderPath = propertiesExtractor.getProperty("s3upload.input-folder-path");
				logger.info("backupFolderPath :" + backupFolderPath);
				File backupFolder = new File(backupFolderPath);
				List<FileDetails> fileDataForUpload = FileDataProvider.getFileDataForUpload(backupFolder);
				fileDataForUpload.forEach(fileDetail -> {
					String key = fileDetail.getFilePathInS3();
					//String key = backupFolder.toPath().relativize(path).toString();
					//key = backupFolderPath.substring(3) + "/" + key;
					//key = key.replace("\\", "/");
					logger.info("key: " + key);
					logger.info("path: " + fileDetail.getFilePathOnLocalDrive());
					Path filePath = Path.of(fileDetail.getFilePathOnLocalDrive());
					long size = filePath.toFile().length();
					try {
						System.out.println("File size : " + FileSizeCalculator.getFileSize(size));
						if (size > FILE_SIZE_IN_BYTES) {
							logger.info("Initiating multipart upload...............");
							initiateMultipartUpload(s3Client, BUCKET_NAME, filePath, key, fileDetail.getFileStatus());
						} else {
							uploadFileToS3(s3Client, BUCKET_NAME, key, filePath,fileDetail.getFileStatus());
							// to test the interruption test case
							Thread.sleep(6000);
						}
					} catch (IOException | InterruptedException e) {
						logger.info("Exception in reading files......");
						e.printStackTrace();
					}
				});
				logger.info("Backup completed successfully.........................");
				sendNotificationEmail(FileUploadDetailsService.getFileDetails(), s3Client, BUCKET_NAME, csvFile);
			} catch (Exception e) {
				logger.error("Error occurred during backup: " + e.getMessage());
				e.printStackTrace();
				sendNotificationEmail(FileUploadDetailsService.getFileDetails(), s3Client, BUCKET_NAME, csvFile);
			} finally {
			}
		}
	}

	private static void initiateMultipartUpload(AmazonS3 s3Client, String BUCKET_NAME, Path filePath, String key, String fileStatus)
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
//			if (fileUploadCategory.equals(FileUploadCategory.DIFFERENTIAL_FILES_UPLOAD)) {
			csvFile = FileUploadDetailsService.backupFileData(key, filePath, FileUploadStatus.SUCCESS,
					fileStatus, FileSizeCalculator.getFileSize(size), csvFile, fileUploadCategory);
//			} else {
//				FileUploadDetailsService.backupFileData(key, filePath, FileUploadStatus.SUCCESS, fileStatus.name(),
//						FileSizeCalculator.getFileSize(size), csvFile, fileUploadCategory);
//			}
		} catch (Exception e) {
			// if (fileUploadCategory.equals(FileUploadCategory.DIFFERENTIAL_FILES_UPLOAD))
			// {
			csvFile = FileUploadDetailsService.backupFileData(key, filePath, FileUploadStatus.FAILED, fileStatus,
					FileSizeCalculator.getFileSize(size), csvFile, fileUploadCategory);
			// } else {
//				FileUploadDetailsService.backupFileData(key, filePath, FileUploadStatus.SUCCESS, fileStatus.name(),
//						FileSizeCalculator.getFileSize(size), csvFile, fileUploadCategory);
			// }
			e.printStackTrace();
			logger.error("Error occured during Multipart upload....." + e.getMessage());

		}
	}

	private static void uploadFileToS3(AmazonS3 s3Client, String bucketName, String key, Path filePath,
			String fileStatus) throws IOException {
		logger.info("inside uploadFileToS3() method......");
		logger.info("File Path is :"+filePath.toString());
		long size = Files.size(filePath);
		//String fileStatus = null;

		//fileStatus = fileStatuses.length > 0 ? fileStatuses[0] : FileDataProvider.getFileStatus().name();
		
		logger.info("File status is : "+fileStatus);
		// Here, fileStatuses has kept of type varargs i.e. variable arguments because,
		// if failure files are getting uploaded,
		// we already have the fileStatus as CREATED or MODIFIED from failureFile csv
		// data and
		// if there is differential file upload or fresh file file upload, we hade to
		// derive filestatus from FileDataProvider class
		try {

			PutObjectRequest request = new PutObjectRequest(bucketName, key, filePath.toFile());
			request.withStorageClass(StorageClass.StandardInfrequentAccess);
			s3Client.putObject(request);
			logger.info("Uploaded file: " + filePath);
			// if (fileStatuses.length > 0) {
			// if (fileUploadCategory.equals(FileUploadCategory.DIFFERENTIAL_FILES_UPLOAD))
			// {
			csvFile = FileUploadDetailsService.backupFileData(key, filePath, FileUploadStatus.SUCCESS, fileStatus,
					FileSizeCalculator.getFileSize(size), csvFile, fileUploadCategory);
			// }
			// } else {
			// if (fileUploadCategory.equals(FileUploadCategory.DIFFERENTIAL_FILES_UPLOAD))
			// {
//					csvFile = FileUploadDetailsService.backupFileData(key, filePath, FileUploadStatus.SUCCESS,
//							fileStatus, FileSizeCalculator.getFileSize(size), csvFile,
//							fileUploadCategory);
			// }
			// }
		} catch (Exception e) {
			logger.error("Error uploading file : " + filePath + " - " + e.getMessage());
			logger.error("Exception : ", e);
//			if (fileStatuses.length > 0) {
//				if (fileUploadCategory.equals(FileUploadCategory.DIFFERENTIAL_FILES_UPLOAD)) {
//					csvFile = FileUploadDetailsService.backupFileData(key, filePath, FileUploadStatus.FAILED,
//							fileStatuses[0], FileSizeCalculator.getFileSize(size), csvFile, fileUploadCategory);
//				}
//			} else {
//				if (fileUploadCategory.equals(FileUploadCategory.DIFFERENTIAL_FILES_UPLOAD)) {
//					csvFile = FileUploadDetailsService.backupFileData(key, filePath, FileUploadStatus.FAILED,
//							FileDataProvider.getFileStatus().name(), FileSizeCalculator.getFileSize(size), csvFile,
//							fileUploadCategory);
//				}
//			}
			csvFile = FileUploadDetailsService.backupFileData(key, filePath, FileUploadStatus.FAILED, fileStatus,
					FileSizeCalculator.getFileSize(size), csvFile, fileUploadCategory);

		}
	}

	private static String getCurrentMonthAndYear() {
		return new SimpleDateFormat("MMM-YYYY").format(new Date());
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
			costReport = s3ObjectFetch.getReportFromBucket(s3Client, BUCKET_NAME, COST_REPORT_FILE_NAME);
			logger.info("Fetching usage report file....................");
			usageReport = s3ObjectFetch.getReportFromBucket(s3Client, BUCKET_NAME, USAGE_REPORT_FILE_NAME);
			emailService.sendMail(costReport, usageReport, csvFile);
		}
	}
}

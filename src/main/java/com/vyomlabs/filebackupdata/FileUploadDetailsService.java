package com.vyomlabs.filebackupdata;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import com.vyomlabs.entity.FileBackupDetails;
import com.vyomlabs.entity.FileDetails;
import com.vyomlabs.entity.FileUploadCategory;
import com.vyomlabs.entity.FileUploadStatus;
import com.vyomlabs.util.FileDataProvider;
import com.vyomlabs.util.PropertiesExtractor;

public class FileUploadDetailsService {

	static List<FileBackupDetails> fileDetails = new ArrayList<>();

	FileDataProvider fileDataProvider = new FileDataProvider();

	static PropertiesExtractor propertiesExtractor = new PropertiesExtractor();

	public static List<FileBackupDetails> getFileDetails() {
		return fileDetails;
	}

	public static void setFileDetails(List<FileBackupDetails> fileDetails) {
		FileUploadDetailsService.fileDetails = fileDetails;
	}

	private final static Logger logger = Logger.getLogger(FileUploadDetailsService.class);

	public static File backupFileData(String key, Path filePath, FileUploadStatus uploadStatus, String fileStatus,
			String fileSize, File csvFile, FileUploadCategory fileUploadCategory) throws IOException {
		String localDrivePath = filePath.toString();
		logger.info("Local drive path : " + localDrivePath);
		logger.info("CSV file name : " + csvFile.getName());
		FileBackupDetails fileBackupDetails = new FileBackupDetails();
		fileBackupDetails.setFileName(filePath.getFileName().toString());
		fileBackupDetails.setFilePathOnLocalDrive(filePath.toString());
		fileBackupDetails.setFilePathInS3(key);
		fileBackupDetails.setUploadStatus(uploadStatus.name());
		fileBackupDetails.setFileStatus(fileStatus);
		fileBackupDetails.setUploadDate(formatProperDateTime(new Date()));
		fileBackupDetails.setFileSize(fileSize);
		fileDetails.add(fileBackupDetails);
		if (fileUploadCategory.equals(FileUploadCategory.FRESH_DIFFERENTIAL_FILES_UPLOAD)) {
			return writeDataInCSVFile(fileBackupDetails, getCSVFile(fileUploadCategory));
		} else {
			return writeDataInCSVFile(fileBackupDetails, getCSVFile(fileUploadCategory));
		}

	}

	public static File writeDataInCSVFile(FileBackupDetails fileBackupDetails, File csvFile) {
		try {
			FileWriter fw = new FileWriter(csvFile, true);
			CSVWriter csvWriter = new CSVWriter(fw);
			String[] data = { fileBackupDetails.getFileName(), fileBackupDetails.getFilePathOnLocalDrive(),
					fileBackupDetails.getFilePathInS3(), fileBackupDetails.getUploadDate(),
					fileBackupDetails.getUploadStatus().toString(), fileBackupDetails.getFileStatus().toString(),
					fileBackupDetails.getFileSize() };
			csvWriter.writeNext(data);
			logger.info("completed writing data in csv file : " + csvFile.getName());
			csvWriter.close();
			return csvFile;
		} catch (Exception e) {
			logger.error("Exception caught in writeDataInCSVFile() method......");
			logger.info(e.getMessage(), e);
			e.printStackTrace();
			return csvFile;
		}
	}

	private static String formatProperDateTime(Date uploadDate) {
		StringBuffer stringBuffer = new StringBuffer();
		Date now = uploadDate;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");
		simpleDateFormat.format(now, stringBuffer, new FieldPosition(0));
		return stringBuffer.toString();
	}

	public static boolean checkIfBackupDetailsFileExists() throws IOException {
		String fileName = getCSVFileName(FileUploadCategory.FRESH_DIFFERENTIAL_FILES_UPLOAD);
		File file = new File(Path.of("").toAbsolutePath().toString() + "/" + fileName);
		logger.info("File Path is : " + file.getAbsolutePath());
		boolean result = file.exists();
		return result;
	}

	public static List<FileDetails> getFailureFileDetails(File csvFile) throws CsvValidationException, IOException {
		List<FileBackupDetails> csvRecords = new ArrayList<>();
		logger.info("File Path is  : " + csvFile.getAbsolutePath());

		FileReader fileReader = new FileReader(csvFile);
		CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT);
		
		for (CSVRecord csvRecord : csvParser) {
			FileBackupDetails fileBackupDetails = FileBackupDetails.builder().fileName(csvRecord.get(0))
					.filePathOnLocalDrive(csvRecord.get(1)).filePathInS3(csvRecord.get(2)).uploadDate(csvRecord.get(3))
					.uploadStatus(csvRecord.get(4)).fileStatus(csvRecord.get(5)).fileSize(csvRecord.get(6)).build();
			csvRecords.add(fileBackupDetails);
		}

		csvParser.close();

		logger.info("no of records from csv are : " + csvRecords.size());
		//logger.info("CSV records : " + csvRecords.toString());
		csvRecords.remove(0);
		logger.info("number of records from csv after removing header are : " + csvRecords.size());
		List<FileDetails> failureFileDetails = new ArrayList<>();

		failureFileDetails.addAll(addThePendingAndFailedFiles(csvRecords));

		//logger.info("Failure or Pending file Details : " + failureFileDetails.toString());
		logger.info("No of failure or pending records are : " + failureFileDetails.size());
		return failureFileDetails;
	}

	private static List<FileDetails> addThePendingAndFailedFiles(List<FileBackupDetails> records) throws IOException {

		// List to capture success records
		List<FileDetails> successRecords = new ArrayList<>();

		// filtering success records from csv and adding to list

		records.stream().filter(record -> record.getUploadStatus().equals(FileUploadStatus.SUCCESS.name()))
				.forEach(record -> {
					FileDetails fileDetail = FileDetails.builder().fileName(record.getFileName())
							.filePathInS3(record.getFilePathInS3())
							.filePathOnLocalDrive(record.getFilePathOnLocalDrive()).fileStatus(record.getFileStatus())
							.build();
					successRecords.add(fileDetail);
				});

		logger.info("no of Success Records are : " + successRecords.size());
		//logger.info("Success records are : \n" + successRecords.toString());

		String backupFolderPath = propertiesExtractor.getProperty("s3upload.input-folder-path");

		File backupFolder = new File(backupFolderPath);


		// get all files data for upload..... from FileDataProvider class
		// which contains (SUCCESS records , FAILED records and PENDING records also)
		List<FileDetails> masterFileDataForUpload = FileDataProvider.getFileDataForUpload(backupFolder);

		//logger.info("Master data for upload is : \n" + masterFileDataForUpload.toString());
		logger.info(
				"No of records for master upload before removing success records : " + masterFileDataForUpload.size());
		// Now we are removing all success records so that only pending and failed
		// records remain in list
		masterFileDataForUpload.removeAll(successRecords);

		logger.info(
				"No of records for master upload after removing success records : " + masterFileDataForUpload.size());

		// now add this list to our resultant list and return

		logger.info("No of records for pendingAndFailedFiles after adding records to new list records : "
				+ masterFileDataForUpload.size());

		//logger.info("Pending and failed file records are : " + masterFileDataForUpload.toString());

		return masterFileDataForUpload;
	}

	public static String getCSVFileName(FileUploadCategory fileUploadCategory) {

		String fileName = null;
		switch (fileUploadCategory) {

			case FRESH_DIFFERENTIAL_FILES_UPLOAD: {
				fileName = LocalDateTime.now().getMonth().toString() + "_" + LocalDateTime.now().getYear() + ".csv";
				break;
			}
			case FAILURE_FILES_UPLOAD: {
				fileName = LocalDateTime.now().getMonth().toString() + "_" + LocalDateTime.now().getYear() + "_" + "RERUN"
						+ ".csv";
				break;
			}
		}
		return fileName;

	}

	public static File createCSVFile(FileUploadCategory fileUploadCategory) throws IOException {
		String fileName = getCSVFileName(fileUploadCategory);
		logger.info("FileName for category : " + fileUploadCategory + " is :" + fileName);
		File file = new File(Path.of("").toAbsolutePath().toString() + "\\" + fileName);
		boolean result = file.createNewFile();
		if (result) {
			logger.info("File Created : " + file.getName() + ", at path :" + file.getAbsolutePath());
		} else {
			logger.info("File already exists : " + file.getName() + "\n at path:" + file.getAbsolutePath());
		}
		FileWriter fw = new FileWriter(file, true);
		CSVWriter csvWriter = new CSVWriter(fw);
		String[] columnNames = { "File Name", "File Path on Local Drive", "File Path in S3", "Upload Date",
				"Upload Status", "File Status", "File Size" };
		csvWriter.writeNext(columnNames);
		csvWriter.close();
		return file;
	}

	public static File getCSVFile(FileUploadCategory fileUploadCategory) {
		String fileName = getCSVFileName(fileUploadCategory);
		logger.info("FileName for category (From getCSVFile() method) : " + fileUploadCategory + " is :" + fileName);
		File file = new File(Path.of("").toAbsolutePath().toString() + "\\" + fileName);
		return file;
	}

	public static File getMainCSVFile() {
		String fileName = LocalDateTime.now().getMonth().toString() + "_" + LocalDateTime.now().getYear() + ".csv";
		File file = new File(Path.of("").toAbsolutePath().toString() + "\\" + fileName);
		return file;
	}

}
package com.vyomlabs.filebackupdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import com.vyomlabs.entity.FileBackupDetails;
import com.vyomlabs.entity.FileUploadStatus;

public class FileUploadDetailsService {

	static List<FileBackupDetails> fileDetails = new ArrayList<>();

	public static List<FileBackupDetails> getFileDetails() {
		return fileDetails;
	}

	public static void setFileDetails(List<FileBackupDetails> fileDetails) {
		FileUploadDetailsService.fileDetails = fileDetails;
	}

	private final static Logger logger = Logger.getLogger(FileUploadDetailsService.class);

	public static File backupFileData(String key, Path filePath, FileUploadStatus uploadStatus, String fileStatus,
			String fileSize, File csvFile) throws IOException {
		String localDrivePath = filePath.toString();
		logger.info("Local drive path : " + localDrivePath);
		// Gson gson = new GsonBuilder().setPrettyPrinting().create();
		FileBackupDetails fileBackupDetails = new FileBackupDetails();
		fileBackupDetails.setFileName(filePath.getFileName().toString());
		fileBackupDetails.setFilePathOnLocalDrive(filePath.toString());
		fileBackupDetails.setFilePathInS3(key);
		fileBackupDetails.setUploadStatus(uploadStatus.name());
		fileBackupDetails.setFileStatus(fileStatus);
		fileBackupDetails.setUploadDate(formatProperDateTime(new Date()));
		fileBackupDetails.setFileSize(fileSize);
		fileDetails.add(fileBackupDetails);
		// logger.info(gson.toJson(fileBackupDetails));
		return writeDataInCSVFile(fileBackupDetails, csvFile);
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
			logger.info("completed writing data in csv file.................");
			csvWriter.close();
			return csvFile;
		} catch (Exception e) {
			logger.error("Exception caught in writeDataInCSVFile() method......");
			// logger.info(e.printStackTrace(), e);
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
		String fileName = getCSVFileName();
		File file = new File(Path.of("").toAbsolutePath().toString() + "/" + fileName);
		logger.info("File Path is : "+file.getAbsolutePath());
		boolean result = file.exists();
		//logger.info("Is file present? : " + result);
		return result;
	}

	public static List<FileBackupDetails> getFailureFileDetails() throws CsvValidationException, IOException {
		List<List<String>> records = new ArrayList<List<String>>();
		File file = new File(Path.of("").toAbsolutePath().toString() + "/" + getCSVFileName());
		logger.info("File Path is  : "+file.getAbsolutePath());
		try (BufferedReader br = new BufferedReader(
				new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				records.add(Arrays.asList(values));
			}
		}
		//logger.info("Records from CSV File : " + records.toString());
		List<FileBackupDetails> failureFileDetails = new ArrayList<>();
		for (List<String> record : records) {
			if (records.get(0).equals(record)) {
				continue;
			} else {
				FileBackupDetails fileBackupDetails = new FileBackupDetails(record.get(0), record.get(1), record.get(2),
						record.get(3), record.get(4), record.get(5), record.get(6));
				//logger.info("File Backup Details Object : "+fileBackupDetails.toString());
				//logger.info("File upload status : "+record.get(4));
				//String string = record.get(4);
				if (record.get(4).equals(FileUploadStatus.FAILED.name())) {
					logger.info("Encountered Failed record.........");
					failureFileDetails.add(fileBackupDetails);
				}
			}
		}
		// List<FileBackupDetails> failureDetails = fileDetails.stream().filter(data ->
		// (data.getUploadStatus().equals(FileUploadStatus.FAILED.name())))
		// .toList();
		logger.info("Failure file Details : " + failureFileDetails.toString());
		return failureFileDetails;
	}

	public static String getCSVFileName() {
		String fileName = LocalDateTime.now().getMonth().toString() + "_" + LocalDateTime.now().getYear() + ".csv";
		// File file = new File(Path.of("").toAbsolutePath().toString() + "\\" +
		// fileName);
		return fileName;
	}

	public static File createCSVFile() throws IOException {
		String fileName = getCSVFileName();
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

	public static File getCSVFile() {
		String fileName = getCSVFileName();
		File file = new File(Path.of("").toAbsolutePath().toString() + "\\" + fileName);
		return file;
	}
}

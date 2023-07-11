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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

public class FileUploadDetailsService {

	static List<FileBackupDetails> fileDetails = new ArrayList<>();

	public static List<FileBackupDetails> getFileDetails() {
		return fileDetails;
	}

	public static void setFileDetails(List<FileBackupDetails> fileDetails) {
		FileUploadDetailsService.fileDetails = fileDetails;
	}

	private final static Logger logger = Logger.getLogger(FileUploadDetailsService.class);

	public static void backupFileData(String key, Path filePath, FileUploadStatus uploadStatus, String fileStatus,
			String fileSize) throws IOException {
		String localDrivePath = filePath.toString();
		logger.info("Local drive path : " + localDrivePath);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		FileBackupDetails fileBackupDetails = new FileBackupDetails();
		fileBackupDetails.setFileName(filePath.getFileName().toString());
		fileBackupDetails.setFilePathOnLocalDrive(filePath.toString());
		fileBackupDetails.setFilePathInS3(key);
		fileBackupDetails.setUploadStatus(uploadStatus.name());
		fileBackupDetails.setFileStatus(fileStatus);
		fileBackupDetails.setUploadDate(formatProperDateTime(new Date()));
		fileBackupDetails.setFileSize(fileSize);
		fileDetails.add(fileBackupDetails);
		System.out.println(gson.toJson(fileBackupDetails));
	}

	public static void writeDataInCSVFile() {
		logger.info(fileDetails.toString());
		File file;
		try {
			file = createFile();
			FileWriter fw = new FileWriter(file);
			CSVWriter csvWriter = new CSVWriter(fw);
			List<String[]> data = new ArrayList<String[]>();
			String[] columnNames = { "File Name", "File Path on Local Drive", "File Path in S3", "Upload Date",
					"Upload Status", "File Status", "File Size" };
			data.add(columnNames);
			fileDetails.stream().forEach((fileDetail) -> {
				data.add(new String[] { fileDetail.getFileName(), fileDetail.getFilePathOnLocalDrive(),
						fileDetail.getFilePathInS3(), fileDetail.getUploadDate(),
						fileDetail.getUploadStatus().toString(), fileDetail.getFileStatus().toString(),
						fileDetail.getFileSize() });
			});
			csvWriter.writeAll(data);
			System.out.println("completed writing data in csv file.................");
			csvWriter.close();
		} catch (Exception e) {
			System.out.println("Exception caught in writeDataInExcelFile() method......");
			e.printStackTrace();
		}
	}

	private static String formatProperDateTime(Date uploadDate) {
		StringBuffer stringBuffer = new StringBuffer();
		Date now = uploadDate;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");
		simpleDateFormat.format(now, stringBuffer, new FieldPosition(0));
		logger.info("Date format : " + stringBuffer.toString());
		return stringBuffer.toString();
	}

	private static File createFile() throws IOException {
		String fileName = LocalDateTime.now().getMonth().toString() + "_" + LocalDateTime.now().getYear() + ".csv";
		File file = new File(Path.of("").toAbsolutePath().toString() + "\\" + fileName);
		boolean result = file.createNewFile();
		if (result) {
			System.out.println("File Created : " + file.getName() + ", at path :" + file.getAbsolutePath());
			return file;
		} else {
			System.out.println("File already exists : " + file.getName() + "\n at path:" + file.getAbsolutePath());
			return file;
		}
	}

	public static boolean checkIfBackupDetailsFileExists() {
		String fileName = LocalDateTime.now().getMonth().toString() + "_" + LocalDateTime.now().getYear() + ".csv";
		File file = new File(fileName);
		return file.exists();
	}

	public static List<FileBackupDetails> getFailureFileDetails() throws CsvValidationException, IOException {
		List<List<String>> records = new ArrayList<List<String>>();
		try (BufferedReader br = new BufferedReader(new FileReader(getCSVFileName() + ".csv"))) {
			String line;
			while ((line = br.readLine()) != null) {
				// System.out.println("Single Line : "+line);
				String[] values = line.split(",");
				records.add(Arrays.asList(values));
			}
		}
		fileDetails = new ArrayList<>();
		for (List<String> record : records) {
			if (records.get(0).equals(record)) {
				continue;
			} else {
				FileBackupDetails fileBackupDetails = new FileBackupDetails(record.get(0), record.get(1), record.get(2),
						record.get(3), record.get(4), record.get(5), record.get(6));
				fileDetails.add(fileBackupDetails);
			}
		}
		System.out.println(fileDetails.toString());
		return fileDetails.stream().filter(data -> (data.getUploadStatus().equals(FileUploadStatus.FAILED.name())))
				.toList();
	}

	private static String getCSVFileName() {
		return LocalDateTime.now().getMonth().toString() + "_" + LocalDateTime.now().getYear();
	}
}

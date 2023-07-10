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

	public static void backupFileData(String key, Path filePath, FileUploadStatus uploadStatus, String fileStatus, String fileSize)
			throws IOException {
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println("inside backupSuccessFileData() method (key): " + key);
		System.out.println("inside backupSuccessFileData() method (filePath): " + filePath);
		FileBackupDetails fileBackupDetails = new FileBackupDetails();
		fileBackupDetails.setFileName(filePath.getFileName().toString());
		fileBackupDetails.setFilePathOnLocalDrive(filePath.toString());
		fileBackupDetails.setFilePathInS3(key);
		fileBackupDetails.setUploadStatus(uploadStatus.name());
		fileBackupDetails.setFileStatus(fileStatus);
		fileBackupDetails.setUploadDate(formatProperDateTime(new Date()));
		fileBackupDetails.setFileSize(fileSize);
		// LocalTime now = LocalTime.now().toString();
		// fileBackupDetails.setUploadTime(LocalTime.now().toString());
		fileDetails.add(fileBackupDetails);
		// File file = createFile();
		// writeDataInExcelFile(file,fileDetails);
		// FileInputStream fis = new FileInputStream(file);
//		FileWriter fw = new FileWriter(file, true);
//		fw.write(gson.toJson(fileBackupDetails));
//		fw.append("\n");
//		fw.close();

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
//			Workbook workbook = new XSSFWorkbook();
//			Sheet sheet = workbook.createSheet("File Data");
			String[] columnNames = { "File Name", "File Path on Local Drive", "File Path in S3", "Upload Date",
					"Upload Status", "File Status","File Size" };
			//csvWriter.writeNext(columnNames);
			data.add(columnNames);
			fileDetails.stream().forEach((fileDetail)-> {
				data.add(new String[] {
						fileDetail.getFileName(),
						fileDetail.getFilePathOnLocalDrive(),
						fileDetail.getFilePathInS3(),
						fileDetail.getUploadDate(),
						fileDetail.getUploadStatus().toString(),
						fileDetail.getFileStatus().toString(),
						fileDetail.getFileSize()
						});
			});
			csvWriter.writeAll(data);
//			Font headerFont = workbook.createFont();
//			headerFont.setBold(true);
//			headerFont.setFontHeightInPoints((short) 12);
//			headerFont.setColor(IndexedColors.BLACK.index);
//			// Create a CellStyle with the font
//			CellStyle headerStyle = workbook.createCellStyle();
//			headerStyle.setFont(headerFont);
//			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//			headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
//			// Create the header row
//			Row headerRow = sheet.createRow(0);
//			// Iterate over the column headings to create columns
//			for (int i = 0; i < columnNames.length; i++) {
//				Cell cell = headerRow.createCell(i);
//				cell.setCellValue(columnNames[i]);
//				cell.setCellStyle(headerStyle);
//
//			}
//			// Freeze Header Row
//			sheet.createFreezePane(0, 1);
//			// CreationHelper creationHelper= workbook.getCreationHelper();
//			int rownum = 1;
//			for (FileBackupDetails fileDetail : fileDetails) {
//				Row row = sheet.createRow(rownum++);
//				row.createCell(0).setCellValue(fileDetail.getFileName());
//				row.createCell(1).setCellValue(fileDetail.getFilePathOnLocalDrive());
//				row.createCell(2).setCellValue(fileDetail.getFilePathInS3());
//				row.createCell(3).setCellValue(formatProperDateTime(fileDetail.getUploadDate()));
//				row.createCell(4).setCellValue(fileDetail.getStatus().name());
//				row.createCell(5).setCellValue(fileDetail.getFileStatus().name());
//			}
//			for (int i = 0; i < columnNames.length; i++) {
//				sheet.autoSizeColumn(i);
//			}
//			FileOutputStream fout = new FileOutputStream(file);
//			workbook.write(fout);
//			fout.close();
//			workbook.close();
			System.out.println("completed writing data in csv file.................");
			csvWriter.close();
			//return fileDetails;
		} catch (Exception e) {
			System.out.println("Exception caught in writeDataInExcelFile() method......");
			e.printStackTrace();
			//return fileDetails;
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

//	public static void main(String[] args) throws IOException {
//		System.out.println(FileUploadStatus.FAILED);
//		System.out.println(FileUploadStatus.SUCCESS);
//		System.out.println(FileStatus.CREATED);
//		System.out.println(FileStatus.MODIFIED);
//		createFile();
//	}

	public static boolean checkIfBackupDetailsFileExists() {
		
		String fileName = LocalDateTime.now().getMonth().toString() + "_" + LocalDateTime.now().getYear() + ".csv";
		File file = new File(fileName);
		return file.exists();
	}

	public static List<FileBackupDetails> getFailureFileDetails() throws CsvValidationException, IOException {
		
		List<List<String>> records = new ArrayList<List<String>>();
		try (BufferedReader br = new BufferedReader(new FileReader(LocalDateTime.now().getMonth().toString() + "_" + LocalDateTime.now().getYear() + ".csv"))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	//System.out.println("Single Line : "+line);
		        String[] values = line.split(",");
		        records.add(Arrays.asList(values));
		    }
		}
//		try (CSVReader csvReader = new CSVReader(new FileReader(LocalDateTime.now().getMonth().toString() + "_" + LocalDateTime.now().getYear() + ".csv"));) {
//		    String[] values = null;
//		    while ((values = csvReader.readNext()) != null) {
//		        records.add(Arrays.asList(values));
//		    }
//		}
		//System.out.println("Records from csv : "+records.toString());
		List<FileBackupDetails> failureFileDetails = new ArrayList<>();
		for(List<String> record : records) {
			if(records.get(0).equals(record)) {
				continue;
			}
			else {
				FileBackupDetails fileBackupDetails = new FileBackupDetails(record.get(0),record.get(1),record.get(2),record.get(3),record.get(4),record.get(5),record.get(6));
				if(fileBackupDetails.getUploadStatus().equals(FileUploadStatus.FAILED.name())) {
					failureFileDetails.add(fileBackupDetails);
				}
			}
		}
		System.out.println(failureFileDetails.toString());
		return failureFileDetails;
	}
	
	public static void main(String[] args) throws CsvValidationException, IOException {
		System.out.println(getFailureFileDetails());
	}
}

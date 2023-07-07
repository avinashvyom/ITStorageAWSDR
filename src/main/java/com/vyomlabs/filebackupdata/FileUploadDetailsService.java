package com.vyomlabs.filebackupdata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FileUploadDetailsService {

	static List<FileBackupDetails> fileDetails = new ArrayList<>();
	public static List<FileBackupDetails> getFileDetails() {
		return fileDetails;
	}

	public static void setFileDetails(List<FileBackupDetails> fileDetails) {
		FileUploadDetailsService.fileDetails = fileDetails;
	}

	private final static Logger logger = Logger.getLogger(FileUploadDetailsService.class);

	public static void backupFileData(String key, Path filePath, FileUploadStatus uploadStatus, FileStatus fileStatus)
			throws IOException {
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println("inside backupSuccessFileData() method (key): " + key);
		System.out.println("inside backupSuccessFileData() method (filePath): " + filePath);
		FileBackupDetails fileBackupDetails = new FileBackupDetails();
		fileBackupDetails.setFileName(filePath.getFileName().toString());
		fileBackupDetails.setFilePathOnLocalDrive(filePath.toString());
		fileBackupDetails.setFilePathInS3(key);
		fileBackupDetails.setStatus(uploadStatus);
		fileBackupDetails.setFileStatus(fileStatus);
		fileBackupDetails.setUploadDate(new Date());
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

	public static void writeDataInExcelFile() {
		logger.info(fileDetails.toString());
		File file;
		try {
			file = createFile();
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("File Data");
			String[] columnNames = { "File Name", "File Path on Local Drive", "File Path in S3", "Upload Date",
					"Upload Status", "File Status" };
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 12);
			headerFont.setColor(IndexedColors.BLACK.index);
			// Create a CellStyle with the font
			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.setFont(headerFont);
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
			// Create the header row
			Row headerRow = sheet.createRow(0);
			// Iterate over the column headings to create columns
			for (int i = 0; i < columnNames.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(columnNames[i]);
				cell.setCellStyle(headerStyle);

			}
			// Freeze Header Row
			sheet.createFreezePane(0, 1);
			// CreationHelper creationHelper= workbook.getCreationHelper();
			int rownum = 1;
			for (FileBackupDetails fileDetail : fileDetails) {
				Row row = sheet.createRow(rownum++);
				row.createCell(0).setCellValue(fileDetail.getFileName());
				row.createCell(1).setCellValue(fileDetail.getFilePathOnLocalDrive());
				row.createCell(2).setCellValue(fileDetail.getFilePathInS3());
				row.createCell(3).setCellValue(formatProperDateTime(fileDetail.getUploadDate()));
				row.createCell(4).setCellValue(fileDetail.getStatus().name());
				row.createCell(5).setCellValue(fileDetail.getFileStatus().name());
			}
			for (int i = 0; i < columnNames.length; i++) {
				sheet.autoSizeColumn(i);
			}
			FileOutputStream fout = new FileOutputStream(file);
			workbook.write(fout);
			fout.close();
			workbook.close();
			System.out.println("completed writing data in excel file.................");
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
		//logger.info("Date format : " + stringBuffer.toString());
		return stringBuffer.toString();
	}

	private static File createFile() throws IOException {
		String fileName = LocalDateTime.now().getMonth().toString() + "_" + LocalDateTime.now().getYear() + ".xlsx";
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
		
		String fileName = LocalDateTime.now().getMonth().toString() + "_" + LocalDateTime.now().getYear() + ".xlsx";
		File file = new File(fileName);
		return file.exists();
	}

	public static List<FileBackupDetails> getFailureFileDetails() {
		return fileDetails.stream()
				.filter((file) -> file.getStatus()
						.equals(FileUploadStatus.FAILED)).toList();
	}
}

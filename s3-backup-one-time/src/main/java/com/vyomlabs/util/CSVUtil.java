package com.vyomlabs.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import com.vyomlabs.entity.FileBackupDetails;
import com.vyomlabs.entity.FileUploadStatus;

public class CSVUtil {
	
	private File csvFile;
	
	private final Logger logger = Logger.getLogger(CSVUtil.class);

	

	public CSVUtil(File file) {
		super();
		this.csvFile = file;
	}

	public int getFailedFilesCount() throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		
		return getCountOnCondition(FileUploadStatus.FAILED);
	}

	public int getSuccessFilesCount() throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		return getCountOnCondition(FileUploadStatus.SUCCESS);
	}
	
	
	private int getCountOnCondition(FileUploadStatus fileUploadStatus) throws FileNotFoundException, IOException {
		
		List<FileBackupDetails> records = new ArrayList<>();
		// File file = new File(Path.of("").toAbsolutePath().toString() + "/" +
		// getCSVFileName());
		logger.info("File Path is (from CSVUtil class) : " + csvFile.getAbsolutePath());
		
		FileReader fileReader = new FileReader(csvFile);
		CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT);
		for (CSVRecord csvRecord : csvParser) {
			FileBackupDetails fileBackupDetails = FileBackupDetails.builder().fileName(csvRecord.get(0))
					.filePathOnLocalDrive(csvRecord.get(1)).filePathInS3(csvRecord.get(2)).uploadDate(csvRecord.get(3))
					.uploadStatus(csvRecord.get(4)).fileStatus(csvRecord.get(5)).fileSize(csvRecord.get(6)).build();
			records.add(fileBackupDetails);
			//csvRecord.stream().filter(record -> record.get(4).equals(fileUploadStatus.name()));
		}

		csvParser.close();
		fileReader.close();
//		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
//			String line;
//			while ((line = br.readLine()) != null) {
//				String[] values = line.split(",");
//				records.add(Arrays.asList(values));
//			}
//		}
		
		int filteredRecords = records.stream().filter(record -> record.getUploadStatus().equals(fileUploadStatus.name())).collect(Collectors.toList()).size();
		logger.info("no of filtered Records are : " + filteredRecords);
		return filteredRecords;
	}
	
	

}

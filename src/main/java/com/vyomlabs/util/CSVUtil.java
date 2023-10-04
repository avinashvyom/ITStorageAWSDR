package com.vyomlabs.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

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
		
		List<List<String>> records = new ArrayList<List<String>>();
		// File file = new File(Path.of("").toAbsolutePath().toString() + "/" +
		// getCSVFileName());
		logger.info("File Path is (from CSVUtil class) : " + csvFile.getAbsolutePath());
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				records.add(Arrays.asList(values));
			}
		}
		
		int filteredRecords = records.stream().filter(record -> record.get(4).equals(fileUploadStatus.name())).collect(Collectors.toList()).size();
		logger.info("Filtered Records are : " + filteredRecords);
		return filteredRecords;
	}
	
	

}

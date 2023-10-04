package com.vyomlabs.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.vyomlabs.entity.FileDetails;
import com.vyomlabs.entity.FileStatus;

public class FileDataProvider {
	
	private final static Logger logger = Logger.getLogger(FileDataProvider.class);
	
	private static FileStatus fileStatus;
	
	public static FileStatus getFileStatus() {
		return fileStatus;
	}

	public static void setFileStatus(FileStatus fileStatus) {
		FileDataProvider.fileStatus = fileStatus;
	}

	static PropertiesExtractor propertiesExtractor = new PropertiesExtractor();
	
	public static List<FileDetails> getFileDataForUpload(File backupFolder) throws IOException {
		// TODO Auto-generated method stub
		
		List<FileDetails> fileData = new ArrayList<>(); 
		 Files.walk(backupFolder.toPath()).filter(path -> !Files.isDirectory(path))
				.filter(path -> isRecentlyUpdated(path)).forEach(path-> {
					String key = backupFolder.toPath().relativize(path).toString();
					key = backupFolder.toPath().toString().substring(3) + "/" + key;
					key = key.replace("\\", "/");
					logger.info("Key is : " + key);
					FileDetails fileDetail = FileDetails.builder()
								.fileName(path.getFileName().toString())
								.filePathOnLocalDrive(path.toString())
								.filePathInS3(key)
								.fileStatus(getFileStatus(path))
								.build();
					fileData.add(fileDetail);
				});
		 
		return fileData;
	}
	
	private static String getFileStatus(Path path) {
		// TODO Auto-generated method stub
		try {
		BasicFileAttributes fileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
		Instant lastModified = fileAttributes.lastModifiedTime().toInstant();
		Instant lastCreated = fileAttributes.creationTime().toInstant();
		if (lastModified.compareTo(lastCreated) == 0) {
			fileStatus = FileStatus.CREATED;
			//setFileStatus(FileStatus.CREATED);
			logger.info("File Status for " + path.toFile().getName() + " is :" + fileStatus);
		} else {
			fileStatus = FileStatus.MODIFIED;
			//setFileStatus(FileStatus.MODIFIED);
			logger.info("File Status for " + path.toFile().getName() + " is :" + fileStatus);
		}
		
		return fileStatus.name();
		}
		catch (Exception e) {
			logger.error("Error retrieving file attributes: " + path + " - " + e.getMessage());
			return null;
		}
	}

	private static boolean isRecentlyUpdated(Path filePath) {
		try {
			BasicFileAttributes fileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);
			Instant lastModified = fileAttributes.lastModifiedTime().toInstant();
			//Instant lastCreated = fileAttributes.creationTime().toInstant();
			int fileUploadDuration = Integer.parseInt(propertiesExtractor.getProperty("files.upload.duration"));
			Instant oneMonthAgo = Instant.now().minus(fileUploadDuration, ChronoUnit.DAYS);
			return lastModified.isAfter(oneMonthAgo);
		} catch (Exception e) {
			logger.error("Error retrieving file attributes: " + filePath + " - " + e.getMessage());
			return false;
		}
	}



}

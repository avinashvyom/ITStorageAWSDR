package com.vyomlabs.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.collections.FastArrayList;
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

	static int fileUploadDuration = Integer.parseInt(propertiesExtractor.getProperty("files.upload.duration"));

	public static List<FileDetails> getFileDataForUpload(File backupFolder) throws IOException {
		// TODO Auto-generated method stub
		List<FileDetails> fileData = new ArrayList<>();
		//FastArrayList<FileDetails> fileData1 = new FastArrayList();
		try {
			logger.info("Inside method getFileDataForUpload() ");
			Files.walkFileTree(backupFolder.toPath(), EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
					new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
							// if (!Files.isDirectory(path) && isRecentlyUpdated(path)) {
							if (!Files.isDirectory(path)) {
								logger.info("Entered in for of getFileDataForUpload()");
								String key = backupFolder.toPath().relativize(path).toString();
								key = backupFolder.toPath().toString().substring(3) + "/" + key;
								key = key.replace("\\", "/");
								logger.info("Key is (getFileDataForUpload method): " + key);
								FileDetails fileDetail = FileDetails.builder().fileName(path.getFileName().toString())
										.filePathOnLocalDrive(path.toString()).filePathInS3(key)
										.fileStatus(getFileStatus(path)).build();
								fileData.add(fileDetail);
							}
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
							// Handle the case where visiting a file fails
							logger.error("Failed to visit file: " + file);
							// Optionally, you can throw the exception to stop the traversal
							// throw exc;
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
								throws IOException {
							// Optionally, do something with the directory before visiting its contents
							logger.info("Pre-visiting directory: " + dir);
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
							// Executed after all the entries in a directory are visited
							if (exc == null) {
								logger.info("Post-visiting directory: " + dir);
							} else {
								// Handle the case where an exception occurred during directory traversal
								System.err.println("Failed to visit directory: " + dir);
								// Optionally, throw the exception to stop the traversal
								// throw exc;
							}
							return FileVisitResult.CONTINUE;
						}
					});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileData;
	}

	public static String getFileStatus(Path path) {
		// TODO Auto-generated method stub
		try {
			BasicFileAttributes fileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
			Instant lastModified = fileAttributes.lastModifiedTime().toInstant();
			Instant lastCreated = fileAttributes.creationTime().toInstant();
			if (lastModified.compareTo(lastCreated) == 0) {
				fileStatus = FileStatus.CREATED;
				// setFileStatus(FileStatus.CREATED);
				// logger.info("File Status for " + path.toFile().getName() + " is :" +
				// fileStatus);
			} else {
				fileStatus = FileStatus.MODIFIED;
				// setFileStatus(FileStatus.MODIFIED);
				// logger.info("File Status for " + path.toFile().getName() + " is :" +
				// fileStatus);
			}

			return fileStatus.name();
		} catch (Exception e) {
			logger.error("Error retrieving file attributes: " + path + " - " + e.getMessage());
			return null;
		}
	}

//	private static boolean isRecentlyUpdated(Path filePath) {
//		try {
//			BasicFileAttributes fileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);
//			Instant lastModified = fileAttributes.lastModifiedTime().toInstant();
//			// Instant lastCreated = fileAttributes.creationTime().toInstant();
//
//			// logger.info("File upload duration is : " + fileUploadDuration);
//			Instant oneMonthAgo = Instant.now().minus(fileUploadDuration, ChronoUnit.DAYS);
//			boolean result = lastModified.isAfter(oneMonthAgo);
//			logger.info("Is file modified during the time duration?:- " + result);
//			return result;
//		} catch (Exception e) {
//			logger.error("Error retrieving file attributes: " + filePath + " - " + e.getMessage());
//			return false;
//		}
//	}

}

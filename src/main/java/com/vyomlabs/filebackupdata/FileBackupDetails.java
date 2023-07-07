package com.vyomlabs.filebackupdata;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileBackupDetails {
	@SerializedName("name")
	private String fileName;
	
	@SerializedName("pathOnLocalDrive")
	private String filePathOnLocalDrive;
	
	@SerializedName("pathOnS3")
	private String filePathInS3;
	
	@SerializedName("uploadDateAndTime")
	private Date uploadDate;
		
	@SerializedName("uploadStatus")
	private FileUploadStatus status;
	
	@SerializedName("fileStatus")
	private FileStatus fileStatus;
}

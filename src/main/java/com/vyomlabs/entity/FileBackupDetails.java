package com.vyomlabs.entity;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
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
	private String uploadDate;
		
	@SerializedName("uploadStatus")
	private String uploadStatus;
	
	@SerializedName("fileStatus")
	private String fileStatus;
	
	@SerializedName("fileSize")
	private String fileSize;
}

package com.vyomlabs.entity;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FileBackupDetails [fileName=");
		builder.append(fileName);
		builder.append(", filePathOnLocalDrive=");
		builder.append(filePathOnLocalDrive);
		builder.append(", filePathInS3=");
		builder.append(filePathInS3);
		builder.append(", uploadDate=");
		builder.append(uploadDate);
		builder.append(", uploadStatus=");
		builder.append(uploadStatus);
		builder.append(", fileStatus=");
		builder.append(fileStatus);
		builder.append(", fileSize=");
		builder.append(fileSize);
		builder.append("]");
		builder.append("\n");
		return builder.toString();
	}

	@SerializedName("fileSize")
	private String fileSize;
}

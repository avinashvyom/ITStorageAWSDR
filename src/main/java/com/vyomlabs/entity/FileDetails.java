package com.vyomlabs.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class FileDetails {


	private String fileName;

	private String filePathOnLocalDrive;

	private String filePathInS3;
	
	private String fileStatus;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FileDetails [fileName=");
		builder.append(fileName);
		builder.append(", filePathOnLocalDrive=");
		builder.append(filePathOnLocalDrive);
		builder.append(", filePathInS3=");
		builder.append(filePathInS3);
		builder.append(", fileStatus=");
		builder.append(fileStatus);
		builder.append("]");
		builder.append("\n");
		return builder.toString();
	}
	
	
	

}

package com.vyomlabs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

public class PropertiesExtractor {
	public File file;
	public PropertiesExtractor() {
		file = new File(Paths.get("").toAbsolutePath().toString() + "\\" + "application.properties");
		try {
			boolean result = file.createNewFile();
			if(result) {
				FileInputStream fis = new FileInputStream(file);
				Properties properties = new Properties();
				properties.load(fis);
				properties.setProperty("s3.secret-key", "S3_SECRET_KEY");
				properties.setProperty("s3.access-key", "S3_ACCESS_KEY");
				properties.setProperty("s3.bucket-name", "S3_BUCKET_NAME");
				properties.setProperty("s3.region", "S3_REGION");
				properties.setProperty("s3upload.input-folder-path", "INPUT_FOLDER_PATH");
				properties.setProperty("mail.smtp.host", "SMTP_HOST");
				properties.setProperty("mail.smtp.port", "SMTP_PORT");
				properties.setProperty("mail.smtp.ssl.enable", "true");
				properties.setProperty("mail.smtp.auth", "true");
				FileOutputStream fos = new FileOutputStream(file);
				properties.store(fos, null);
				fos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getProperty(String key) {
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			Properties properties = new Properties();
			properties.load(fis);
			return properties.getProperty(key);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}

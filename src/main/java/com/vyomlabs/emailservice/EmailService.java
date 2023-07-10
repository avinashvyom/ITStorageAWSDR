package com.vyomlabs.emailservice;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.vyomlabs.filebackupdata.FileBackupDetails;
import com.vyomlabs.filebackupdata.FileUploadStatus;
import com.vyomlabs.util.PropertiesExtractor;
import com.vyomlabs.util.TextEncryptorAndDecryptor;

public class EmailService {

	List<FileBackupDetails> fileList = new ArrayList<>();

	PropertiesExtractor propertiesExtractor = new PropertiesExtractor();

	public List<FileBackupDetails> getFileList() {
		return fileList;
	}

	public void setFileList(List<FileBackupDetails> fileList) {
		this.fileList = fileList;
	}

	public void sendMail(File costReport, File usageReport) throws IOException {
		// TODO Auto-generated method stub

		System.out.println("preparing to send message ...");
		String message = composeMessage(fileList);
		String subject = "Monthly S3 Bucket Usage and Cost Report for the Month - " + new SimpleDateFormat("MMM/YYYY").format(new Date());
		String to = propertiesExtractor.getProperty("mail.receiver");
		String from = propertiesExtractor.getProperty("mail.sender");
		;// "backup@vyommail.com";//"aws-storage@vyom-labs.com";//
			// "prasad.dharmadhikari@vyomlabs.com";
		String host = propertiesExtractor.getProperty("mail.smtp.host");// "10.51.4.50";
		// String smtpServer =
		// "smtp.yandex.com";//"vyomlabs-com.mail.protection.outlook.com";

		// get the system properties
		Properties properties = System.getProperties();
		System.out.println("PROPERTIES " + properties);
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", propertiesExtractor.getProperty("mail.smtp.port"));
		properties.put("mail.smtp.starttls.enable", propertiesExtractor.getProperty("mail.smtp.starttls.enable"));
		// properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.auth", propertiesExtractor.getProperty("mail.smtp.auth"));
		// Session.getInstance(properties);
		// Session session = Session.getInstance(properties);
		String password = TextEncryptorAndDecryptor
				.decrypt(propertiesExtractor.getProperty("mail.authentication.password"));
		Session session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(from, password);
			}
		});
		session.setDebug(true);

		MimeMessage mimeMessage = new MimeMessage(session);

		try {
			mimeMessage.setFrom(from);
			mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			mimeMessage.setSubject(subject);
			//String path = "D:\\Vyom Projects\\DR setup AWS S3\\FileBackupToAWSS3\\JULY_2023.xlsx";

			MimeMultipart mimeMultipart = new MimeMultipart();
			// text
			// file

			MimeBodyPart textMime = new MimeBodyPart();
			// IOUtils.copy(null, null);
			MimeBodyPart costFileMime = new MimeBodyPart();
			MimeBodyPart usageFileMime = new MimeBodyPart();
			textMime.setText(message);
			costFileMime.attachFile(costReport);
			usageFileMime.attachFile(usageReport);
			mimeMultipart.addBodyPart(textMime);
			mimeMultipart.addBodyPart(costFileMime);
			mimeMultipart.addBodyPart(usageFileMime);
			mimeMessage.setContent(mimeMultipart);
			Transport.send(mimeMessage);
			System.out.println("Mail sent successfully.........................");
		} catch (Exception e) {
			System.err.println("Error occured during sending mail....................");
			e.printStackTrace();
		}

	}

	private String composeMessage(List<FileBackupDetails> fileList) {
		StringBuffer sb = new StringBuffer();
		sb.append("Dear Admin , \n\n");
		sb.append("We hope this email finds you well. We are writing to provide you with the monthly AWS S3 bucket cost and usage report for the month ")
			.append(new SimpleDateFormat("MMM/YYYY").format(new Date()))
			.append(". \nThis report aims to summarize the usage cost incurred and data usage in this month, and details about the number of objects stored, bucket size and cost of the S3 bucket. \n\n")
			.append("\nAttention to these details will greatly contribute to our overall understanding of S3 bucket operations and assist in decision-making processes.\n\n");
		sb.append("Following are the details of files uploaded this month: \n");
		int failedFiles = (int) fileList.stream().filter((name) -> name.getUploadStatus().equals(FileUploadStatus.FAILED.name()))
				.count();
		int successFiles = (int) fileList.stream().filter((name) -> name.getUploadStatus().equals(FileUploadStatus.SUCCESS.name()))
				.count();
		int totalFiles = successFiles + failedFiles;
		sb.append("Total number of files  : " + totalFiles + "\n");
		sb.append("Number of files that are successfully uploaded : " + successFiles + "\n");
		sb.append("Number of files that couldn't upload  : " + failedFiles + "\n");
		sb.append("\n");
		sb.append("\n");
		sb.append("\n");
		sb.append("\n");
		sb.append("\n");
		sb.append("Thanks and Regards,\n");
		sb.append("Vyom Labs Backup Team");
		return sb.toString();
	}

//	public static void main(String[] args) throws IOException {
//		EmailService emailService = new EmailService();
//		emailService.sendMail();
//	}
}

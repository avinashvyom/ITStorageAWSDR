package com.vyomlabs.emailservice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import com.vyomlabs.entity.FileBackupDetails;
import com.vyomlabs.util.CSVUtil;
import com.vyomlabs.util.PropertiesExtractor;
import com.vyomlabs.util.TextEncryptorAndDecryptor;

public class EmailService {
	
	private final static Logger logger = Logger.getLogger(EmailService.class);

	List<FileBackupDetails> fileList = new ArrayList<>();

	PropertiesExtractor propertiesExtractor = new PropertiesExtractor();

	public List<FileBackupDetails> getFileList() {
		return fileList;
	}

	public void setFileList(List<FileBackupDetails> fileList) {
		this.fileList = fileList;
	}

	public void sendMail(File costReport, File usageReport, File csvFile) throws IOException {
		//PropertyConfigurator.configure("src/main/resources/log4j.properties");

		logger.info("preparing to send message ...");
		String message = composeMessage(csvFile);
		String subject = "Monthly S3 Bucket Usage and Cost Report for the Month - " + new SimpleDateFormat("MMM/YYYY").format(new Date());
		String to = propertiesExtractor.getProperty("mail.receiver");
		String from = propertiesExtractor.getProperty("mail.sender");
		String host = propertiesExtractor.getProperty("mail.smtp.host");// "10.51.4.50";

		Properties properties = System.getProperties();
		//logger.info("PROPERTIES : " + properties);
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", propertiesExtractor.getProperty("mail.smtp.port"));
		properties.put("mail.smtp.starttls.enable", propertiesExtractor.getProperty("mail.smtp.starttls.enable"));
		properties.put("mail.smtp.auth", propertiesExtractor.getProperty("mail.smtp.auth"));
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
			Arrays.asList(to.split(",")).forEach((mail)->{
				try {
					if(isValidEmail(mail)) {
						mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(mail));
					}
					else {
						logger.info("invalid email id.......");
					}
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			});
			mimeMessage.setSubject(subject);
			
			MimeMultipart mimeMultipart = new MimeMultipart();
			MimeBodyPart textMime = new MimeBodyPart();
			MimeBodyPart costFileMime = new MimeBodyPart();
			MimeBodyPart usageFileMime = new MimeBodyPart();
			MimeBodyPart fileDetailsMime = new MimeBodyPart();
			textMime.setText(message);
			costFileMime.attachFile(costReport);
			usageFileMime.attachFile(usageReport);
			fileDetailsMime.attachFile(csvFile);
			mimeMultipart.addBodyPart(textMime);
			mimeMultipart.addBodyPart(costFileMime);
			mimeMultipart.addBodyPart(usageFileMime);
			mimeMultipart.addBodyPart(fileDetailsMime);
			mimeMessage.setContent(mimeMultipart);
			Transport.send(mimeMessage);
			logger.info("Mail sent successfully.........................");
		} catch (Exception e) {
			logger.error("Error occured during sending mail....................");
			e.printStackTrace();
		}
	}

	private boolean isValidEmail(String mail) {
		String patternForEmail = "^[a-z0-9](\\.?[a-z0-9_-]){0,}@[a-z0-9-]+\\.([a-z]{1,6}\\.)?[a-z]{2,6}$";
		Pattern pattern = Pattern.compile(patternForEmail);
		Matcher matcher = pattern.matcher(mail);
		return matcher.matches();
	}

	private String composeMessage(File csvFile) throws FileNotFoundException, IOException {
		StringBuffer sb = new StringBuffer();
		sb.append("Dear Admin , \n\n");
		sb.append("We hope this email finds you well. We are writing to provide you with the monthly AWS S3 bucket cost and usage report for the month ")
				.append(new SimpleDateFormat("MMM/YYYY").format(new Date()))
				.append(". \nThis report aims to summarize the usage cost incurred and data usage in this month, and details about the number of objects stored, bucket size and cost of the S3 bucket. \n\n")
				.append("\nAttention to these details will greatly contribute to our overall understanding of S3 bucket operations and assist in decision-making processes.\n\n");
		sb.append("Following are the details of files uploaded this month: \n");
		
		CSVUtil csvUtil = new CSVUtil(csvFile);
		int failedFiles = csvUtil.getFailedFilesCount();
		int successFiles = csvUtil.getSuccessFilesCount();
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

}

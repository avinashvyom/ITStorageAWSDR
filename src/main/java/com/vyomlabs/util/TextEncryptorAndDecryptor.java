package com.vyomlabs.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class TextEncryptorAndDecryptor {
	private static SecretKeySpec secretKey;
	private static byte[] key;
	private static String keyForEncryptionAndDecryption = "vyomlabs";

	// setKey
	public static void setKey(String myKey) {
		try {
			key = myKey.getBytes("UTF-8");
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			secretKey = new SecretKeySpec(key, "AES");
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// encrypt the data
	public static String encrypt(String inputString) {
		try {
			setKey(keyForEncryptionAndDecryption);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.getEncoder().encodeToString(cipher.doFinal(inputString.getBytes("UTF-8")));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	// decrypt the data
	public static String decrypt(String encryptedText) {
		try {
			setKey(keyForEncryptionAndDecryption);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedText)));
			// return
			// Base64.getEncoder().encodeToString(cipher.doFinal(inputString.getBytes("UTF-8")));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String encryptedText = encrypt("AKIA46KGYVBTDKOMTCXQ");
		System.out.println("Encrypted text is : " + encryptedText);

		String originalString = decrypt(encryptedText);

		System.out.println("Original string is : " + originalString);

	}

}

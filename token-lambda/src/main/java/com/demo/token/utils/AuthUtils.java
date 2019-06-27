package com.demo.token.utils;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.stereotype.Component;

@Component
public class AuthUtils {

	private final String SECRET_KEY_GEN_ALGO = "PBKDF2WithHmacSHA512";
	private final String SECURE_RANDOM_ALGO = "SHA1PRNG";
	private final int ITERATIONS = 10000;
	private final int KEY_LENGTH = 512;
	
	public String generateHash(String key) {
		String hash = key;
		try {

			char[] keyChars = key.toCharArray();
			byte[] saltBytes = generateSalt();

			SecretKeyFactory skf = SecretKeyFactory .getInstance(SECRET_KEY_GEN_ALGO);
			PBEKeySpec spec = new PBEKeySpec(keyChars, saltBytes, ITERATIONS, KEY_LENGTH );
			SecretKey secretKey = skf.generateSecret(spec);
			byte[] encoded = secretKey.getEncoded();
			hash = ITERATIONS+":"+toHexFromByte(saltBytes)+":"+toHexFromByte(encoded);

		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return hash;
	}

	public boolean validatePassword(String inputPass, String storedPass) {
		int diff = 1; /* Set default to failed condition*/
		try {
			String[] split = storedPass.split(":");
			int iterations = Integer.parseInt(split[0]);
			byte[] salt = fromHexToByte(split[1]);
			byte[] hash = fromHexToByte(split[2]);
			PBEKeySpec spec = new PBEKeySpec(inputPass.toCharArray(), salt, iterations, hash.length * 8);
			SecretKeyFactory skf = SecretKeyFactory.getInstance(SECRET_KEY_GEN_ALGO);
			byte[] inputHash = skf.generateSecret(spec).getEncoded();

			/* First check XOR for length*/
			diff = hash.length ^ inputHash.length;
			if(diff==0) {
				for(int i = 0; i < hash.length && i < inputHash.length; i++)
				{
					diff |= hash[i] ^ inputHash[i];
				}
			}
		}catch (NoSuchAlgorithmException |InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return diff==0;
	}

	private byte[] generateSalt() throws NoSuchAlgorithmException {
		SecureRandom sr = SecureRandom.getInstance(SECURE_RANDOM_ALGO);
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		return salt;
	}

	private static String toHexFromByte(byte[] array) throws NoSuchAlgorithmException
	{
		BigInteger bi = new BigInteger(1, array);
		String hex = bi.toString(16);
		int paddingLength = (array.length * 2) - hex.length();
		if(paddingLength > 0)
		{
			return String.format("%0"  +paddingLength + "d", 0) + hex;
		}else{
			return hex;
		}
	}

	private static byte[] fromHexToByte(String hex) throws NoSuchAlgorithmException
	{
		byte[] bytes = new byte[hex.length() / 2];
		for(int i = 0; i<bytes.length ;i++)
		{
			bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return bytes;
	}

}

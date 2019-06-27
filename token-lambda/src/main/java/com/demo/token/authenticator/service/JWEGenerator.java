package com.demo.token.authenticator.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.demo.token.dao.UserInfo;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;

@Component
public class JWEGenerator {

	@Value("${spring.application.name}")
	private String appName;

	@Value("${tokenExpiryInterval}")
	private long tokenExpiryInterval;

	public String generateJWE(Map<String, String> data, UserInfo userInfo) throws Exception {
		JWTClaimsSet jwtClaimSet = buildClaimSet(data, userInfo);
		JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128CBC_HS256);
		// Create the encrypted JWT object
		EncryptedJWT jwt = new EncryptedJWT(header, jwtClaimSet);
		RSAEncrypter encrypter = null;
		RSAPublicKey publicKey = getPublicKey("public.pem");
		encrypter = new RSAEncrypter(publicKey);
		jwt.encrypt(encrypter);
		return jwt.serialize();
	}


	public JWTClaimsSet buildClaimSet(Map<String, String> data, UserInfo userInfo) {
		Date now = new Date();
		String[] arn = userInfo.getArn().split(":");
		JWTClaimsSet.Builder jwtClaimsBuilder = new JWTClaimsSet.Builder()
				.issuer(appName)
				.subject(userInfo.getUserName())
				.audience(Arrays.asList("AccountsMS", "CustomerMS"))
				.expirationTime(new Date(now.getTime() + tokenExpiryInterval)) // expires in 10 minutes
				.notBeforeTime(now)
				.issueTime(now)
				.claim("region_created", arn[3]) // adding AWS region fetched from AWS context
				.claim("region_latest", arn[3]) // adding AWS region fetched from AWS context
				.claim("userName", userInfo.getUserName()) // this will be validated against Redis cache
				.claim("role", "CUSTOMER_ROLE") // this is default role for all users
				.claim("userName-key", userInfo.getRedisKey()) // this is session key for Redis
				.jwtID(UUID.randomUUID().toString())
				;

		if (data != null && !data.isEmpty()) {
			for (Map.Entry<String, String> e : data.entrySet()) {
				jwtClaimsBuilder.claim(e.getKey(), e.getValue());
			}
		}
		return jwtClaimsBuilder.build();
	}

	private static String getKey(String filename) throws IOException {
		// Read key from file
		String strKeyPEM = "";
		ClassPathResource  classPathResource = new ClassPathResource(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(classPathResource.getInputStream()));
		String line;
		while ((line = br.readLine()) != null) {
			strKeyPEM += line + "\n";
		}
		br.close();
		return strKeyPEM;
	}

	public static RSAPublicKey getPublicKeyFromString(String key) throws IOException, GeneralSecurityException {
		String publicKeyPEM = key;
		publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----\n", "");
		publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
		byte[] decoded = Base64.decodeBase64(publicKeyPEM);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(decoded));
		return pubKey;
	}

	public static RSAPublicKey getPublicKey(String filename) throws IOException, GeneralSecurityException {
		String publicKeyPEM = getKey(filename);
		return getPublicKeyFromString(publicKeyPEM);
	}


}
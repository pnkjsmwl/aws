package com.demo.token.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;

@Component
public class JWEValidator{
	private Log log = LogFactory.getLog(JWEValidator.class);

	@Value("${tokenExpiryInterval:600000}")
	private long tokenExpiryInterval;

	public JWTPayload getJWTPayload(String JWEToken) throws ParseException, IllegalAccessException, NoSuchAlgorithmException, InvalidKeySpecException, IOException, JOSEException {
		EncryptedJWT jwt = EncryptedJWT.parse(JWEToken);
		RSAPrivateKey privateKey = getPrivateKey("private_key.pem");
		RSADecrypter decrypter = new RSADecrypter(privateKey);
		jwt.decrypt(decrypter);
		JWTPayload payload = new JWTPayload();
		JWTClaimsSet jwtClaimsSet = jwt.getJWTClaimsSet();
		payload.setUserName(jwtClaimsSet.getStringClaim("userName"));
		payload.setRole(jwtClaimsSet.getStringClaim("role"));
		payload.setExpirationTime(jwtClaimsSet.getExpirationTime());
		payload.setIssuer(jwtClaimsSet.getIssuer());
		payload.setAccountNumber(jwtClaimsSet.getStringClaim("accountNumber"));
		log.info("UserName from token : "+payload.getUserName());


		if(! verifyTokenInfo(payload)) 
			throw new IllegalAccessException("JWT token validation failed !!!");

		return payload;
	}

	public boolean verifyTokenInfo(JWTPayload payload){
		Date now = new Date();
		if (now.getTime() - payload.getExpirationTime().getTime()  > tokenExpiryInterval){
			log.info("JWT token expired !!!");
			return false;
		}
		if (! payload.getIssuer().equals("AuthenticatorMS")){
			log.info("Invalid issuer of JWT token !!!");
			return false;
		}
		return true;
	}
	public RSAPrivateKey getPrivateKey(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		String privateKeyPEM = getKey(filename);
		return getPrivateKeyFromString(privateKeyPEM);
	}

	public static RSAPrivateKey getPrivateKeyFromString(String key) throws NoSuchAlgorithmException, InvalidKeySpecException  {
		String privateKeyPEM = key;
		privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----\n", "");
		privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
		byte[] encoded = Base64.decodeBase64(privateKeyPEM);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
		RSAPrivateKey privKey = (RSAPrivateKey) kf.generatePrivate(keySpec);
		return privKey;
	}

	private static String getKey(String filename) throws IOException  {
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

	public boolean checkPolicy(JWTPayload jwtPayload, String method, String requestURI, Map<String, List<String>> policyMap) {
		String[] split = requestURI.split("/");
		String requestAPI = split[1];
		String request = split[2];

		String user_role = jwtPayload.getRole();
		if("account".equals(requestAPI)) {
			if("summary".equals(request)  && policyMap.get("account_summary").contains(user_role) )
				return true;
		}
		else if("customer".equals(requestAPI)) {
			if("summary".equals(request)  && policyMap.get("profile_summary").contains(user_role) )
				return true;
		}
		else if("transaction".equals(requestAPI)) {
			if("summary".equals(request)  && policyMap.get("transaction_summary").contains(user_role) )
				return true;
			else if("recent".equals(request)  && policyMap.get("transaction_recent").contains(user_role) )
				return true;
			else if("last".equals(request)  && policyMap.get("transaction_last").contains(user_role) )
				return true;
		}

		return false;
	}
}
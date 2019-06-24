package com.demo.token.authenticator.service;

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
import java.util.HashMap;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.demo.token.dao.JWTPayload;
import com.demo.token.utils.RedisUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;

@Component
public class JWEValidator{
	@Value("${tokenExpiryInterval}")
	private long tokenExpiryInterval;

	@Autowired
	@Qualifier("policy")
	private HashMap<String,List<String>> policy;

	@Autowired
	private RedisUtils redisUtils;

	@Value("${token_value}")
	private String tokenValue;

	private Log log = LogFactory.getLog(JWEValidator.class);
	private String errorMessage;

	public JWTPayload validateToken(String JWEToken, String region_current) throws ParseException, NoSuchAlgorithmException, InvalidKeySpecException, IOException, JOSEException, IllegalAccessException {

		JWTPayload payload = extractTokenData(JWEToken);
		System.out.println("Region created : "+payload.getRegionCreated());
		String tokenValueFromRedis = "";

		tokenValueFromRedis = redisUtils.tokenValueFromRedis(payload, region_current);

		//redisUtils.validateAccountFromRedis(payload);
		
		
		if(tokenValueFromRedis==null)
			throw new IllegalAccessException("JWT token validation failed, not found in Cache.");
		else if(!verifyTokenInfo(payload))
			throw new IllegalAccessException("JWT token validation failed, "+errorMessage);
		else if(tokenValue.equals(tokenValueFromRedis))
			payload.setValid(true);
		return payload;
	}

	public JWTPayload extractTokenData(String JWEToken) throws ParseException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {
		EncryptedJWT jwt = EncryptedJWT.parse(JWEToken);
		RSAPrivateKey privateKey = getPrivateKey("private_key.pem");
		RSADecrypter decrypter = new RSADecrypter(privateKey);
		jwt.decrypt(decrypter);

		JWTPayload payload = new JWTPayload();
		JWTClaimsSet jwtClaimsSet = jwt.getJWTClaimsSet();
		payload.setAccountNumber(jwtClaimsSet.getStringClaim("accountNumber"));
		payload.setRole(jwtClaimsSet.getStringClaim("role"));
		payload.setUserName(jwtClaimsSet.getStringClaim("userName"));
		payload.setAudience(jwtClaimsSet.getAudience());
		payload.setExpirationTime(jwtClaimsSet.getExpirationTime());
		payload.setIssueTime(jwtClaimsSet.getIssueTime());
		payload.setIssuer(jwtClaimsSet.getIssuer());
		payload.setJwtID(jwtClaimsSet.getJWTID());
		payload.setSubject(jwtClaimsSet.getSubject());
		payload.setRedisKey(jwtClaimsSet.getStringClaim("userName-key"));
		payload.setRegionCreated(jwtClaimsSet.getStringClaim("region_created"));
		payload.setRegionLatest(jwtClaimsSet.getStringClaim("region_latest"));
		return payload;
	}

	public boolean verifyTokenInfo(JWTPayload payload){
		Date now = new Date();
		if (now.getTime() - payload.getExpirationTime().getTime()  > tokenExpiryInterval){
			errorMessage = "JWT token expired !!!";
			log.error("JWT token expired !!!");
			return false;
		}

		if (! payload.getIssuer().equals("AuthenticatorMS")){
			errorMessage = "Invalid issuer of JWT token !!!";
			log.error("Invalid issuer of JWT token !!!");
			return false;
		}
		/*if (! payload.getAudience().contains(caller.getName())){
			errorMessage = "Invalid caller of JWT token !!!";
			log.error("Invalid caller of JWT token !!!");
			return false;
		}
		String policyKey=caller.getHttpVerb()+caller.getResource();
		if(! policy.containsKey(policyKey)){
			errorMessage = "API is not configured in the policy document !!!";
			log.error("API is not configured in the policy document !!!");
			return false;
		}
		if (! policy.get(policyKey).contains(payload.getRole())){
			errorMessage = "User has insufficient role !!!";
			log.error("User has insufficient role !!!");
			return false;
		}*/

		return true;
	}
	public RSAPrivateKey getPrivateKey(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		String privateKeyPEM = getKey(filename);
		return getPrivateKeyFromString(privateKeyPEM);
	}

	public static RSAPrivateKey getPrivateKeyFromString(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
		String privateKeyPEM = key;
		privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----\n", "");
		privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
		byte[] encoded = Base64.decodeBase64(privateKeyPEM);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
		RSAPrivateKey privKey = (RSAPrivateKey) kf.generatePrivate(keySpec);
		return privKey;
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
}
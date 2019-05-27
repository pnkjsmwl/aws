package com.bhavani.authenticator.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
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

import com.bhavani.authenticator.dao.Caller;
import com.bhavani.authenticator.dao.JWTPayload;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;

import redis.clients.jedis.Jedis;



@Component
public class JWEValidator{
	@Value("${tokenExpiryInterval}")
	private long tokenExpiryInterval;

	@Autowired
	@Qualifier("policy")
	private HashMap<String,List<String>> policy;

	@Autowired
	private Jedis jedis;

	private Log log = LogFactory.getLog(JWEValidator.class);

	public JWTPayload validateToken(String JWEToken,Caller caller) throws Exception{
		EncryptedJWT jwt = EncryptedJWT.parse(JWEToken);
		RSAPrivateKey privateKey = getPrivateKey("private_key.pem");
		RSADecrypter decrypter = new RSADecrypter(privateKey);
		jwt.decrypt(decrypter);

		log.info("Value from redis : "+jedis.get(JWEToken));

		JWTPayload payload = new JWTPayload();
		JWTClaimsSet jwtClaimsSet = jwt.getJWTClaimsSet();
		payload.setAccountNumber(jwtClaimsSet.getStringClaim("accountNumber"));
		payload.setRole(jwtClaimsSet.getStringClaim("role"));
		payload.setAudience(jwtClaimsSet.getAudience());
		payload.setExpirationTime(jwtClaimsSet.getExpirationTime());
		payload.setIssueTime(jwtClaimsSet.getIssueTime());
		payload.setIssuer(jwtClaimsSet.getIssuer());
		payload.setJwtID(jwtClaimsSet.getJWTID());
		payload.setSubject(jwtClaimsSet.getSubject());


		if(! verifyTokenInfo(payload,caller))
			throw new IllegalAccessException("JWT token validation failed !!!");
		return payload;
	}

	public boolean verifyTokenInfo(JWTPayload payload,Caller caller){
		Date now = new Date();
		if (now.getTime() - payload.getExpirationTime().getTime()  > tokenExpiryInterval){
			log.error("JWT token expired !!!");
			return false;
		}

		if (! payload.getIssuer().equals("AuthenticatorMS")){
			log.error("Invalid issuer of JWT token !!!");
			return false;
		}
		if (! payload.getAudience().contains(caller.getName())){
			log.error("Invalid caller of JWT token !!!");
			return false;
		}
		String policyKey=caller.getHttpVerb()+caller.getResource();
		if(! policy.containsKey(policyKey)){
			log.error("API is not configured in the policy document !!!");
			return false;
		}
		if (! policy.get(policyKey).contains(payload.getRole())){
			log.error("User has insufficient role !!!");
			return false;
		}

		return true;
	}
	public RSAPrivateKey getPrivateKey(String filename) throws Exception{
		String privateKeyPEM = getKey(filename);
		return getPrivateKeyFromString(privateKeyPEM);
	}

	public static RSAPrivateKey getPrivateKeyFromString(String key) throws Exception {
		String privateKeyPEM = key;
		privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----\n", "");
		privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
		byte[] encoded = Base64.decodeBase64(privateKeyPEM);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
		RSAPrivateKey privKey = (RSAPrivateKey) kf.generatePrivate(keySpec);
		return privKey;
	}

	private static String getKey(String filename) throws Exception {
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
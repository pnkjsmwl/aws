package com.demo.token.authenticator.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.lambda.runtime.Context;
import com.demo.token.authenticator.exception.InvalidTokenException;
import com.demo.token.authenticator.utils.CommonUtils;
import com.demo.token.dao.APIGatewayProxyRequest;
import com.demo.token.dao.APIGatewayProxyResponse;
import com.demo.token.dao.Credentials;
import com.demo.token.dao.JWTPayload;
import com.demo.token.dao.UserInfo;
import com.demo.token.dao.repo.DynamoDbRepo;
import com.google.gson.Gson;
import com.nimbusds.jose.JOSEException;

@RestController
public class AuthenticatorService  {
	//private Log log = LogFactory.getLog(AuthenticatorService.class);

	private JWEGenerator jweGenerator;
	private JWEValidator jweValidator;
	private DynamoDbRepo dynamoDbRepo;
	private CommonUtils util;
	private Gson gson;

	@Autowired
	public AuthenticatorService(JWEGenerator jweGenerator, JWEValidator jweValidator, DynamoDbRepo dynamoDbRepo, CommonUtils util, Gson gson) {
		this.jweGenerator = jweGenerator;
		this.jweValidator = jweValidator;
		this.dynamoDbRepo = dynamoDbRepo;
		this.util = util;
		this.gson = gson;
	}


	@RequestMapping(value = "/signon", method = RequestMethod.POST)
	public ResponseEntity<String> signon(@RequestBody Credentials credentials) {
		try {
			UserInfo user = dynamoDbRepo.getUserByUserName(credentials.getUserName());
			if (user!=null) {
				if(util.validatePassword(credentials.getPassword(), user.getPassword())){
					user.setArn(credentials.getArn());
					user.setRedisKey(user.getUserName()+":"+UUID.randomUUID().toString());
					String encryptedJWT = jweGenerator.generateJWE(null, user);

					jweGenerator.addToCache(user);

					HttpHeaders header = new HttpHeaders();
					header.add(HttpHeaders.AUTHORIZATION, encryptedJWT);
					header.add("Region", credentials.getArn().split(":")[3]);
					header.add("AccountId", user.getAccountNumber());
					header.add("AccountNumber", getLastNDigit(user.getAccountNumber(), 4));
					return ResponseEntity.ok().headers(header).body("{'Message':'Login Successful'}");

				} else {
					return ResponseEntity.badRequest().body("{'Error':'Invalid Password'}");
				}
			}else {
				return ResponseEntity.badRequest().body("{'Error':'Invalid Username'}");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("{'OOPS!!!  Error Occurred'}");
		}
	}

	@RequestMapping(value = "/signon2", method = RequestMethod.POST)
	public APIGatewayProxyResponse signon2(@RequestBody APIGatewayProxyRequest request, Context context) {
		try {
			Credentials cred = gson.fromJson(request.getBody(), Credentials.class);
			System.out.println("User from request : "+cred.getUserName());
			UserInfo user = dynamoDbRepo.getUserByUserName(cred.getUserName());

			if (user!=null) {
				if(util.validatePassword(cred.getPassword(), user.getPassword())){
					user.setArn(context.getInvokedFunctionArn());
					user.setRedisKey(user.getUserName()+":"+UUID.randomUUID().toString());
					String encryptedJWT = jweGenerator.generateJWE(null, user);

					jweGenerator.addToCache(user);

					System.out.println("ARN : "+user.getArn());

					Map<String, String> header = new HashMap<String,String>();
					header.put(HttpHeaders.AUTHORIZATION, encryptedJWT);
					header.put("Region", user.getArn().split(":")[3]);
					header.put("AccountId", user.getAccountNumber());
					header.put("AccountNumber", getLastNDigit(user.getAccountNumber(), 4));

					return new APIGatewayProxyResponse(200, header, "Message : Login Successful", true);

				} else {
					return new APIGatewayProxyResponse(403, null, "Error : Invalid Password", true);
				}
			}else {
				return new APIGatewayProxyResponse(403, null, "Error : Invalid Username", true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Map<String, String> header2 = new HashMap<String,String>();
			header2.put("Error", e.getMessage());
			return new APIGatewayProxyResponse(500, null, "OOPS !!! Error Occurred", true);
		}
	}

	private String getLastNDigit(String number, int digits) {
		StringBuilder s = new StringBuilder(number); 
		IntStream.rangeClosed(0, number.length()-digits-1).forEach(n -> s.setCharAt(n, '*'));
		return s.toString();
	}

	//@RequestMapping(value = "/token/authorize", method = RequestMethod.POST)
	public String validate(String jwtToken, String invokedFunctionArn) throws Exception {

		try {
			String region_current = invokedFunctionArn.split(":")[3];
			System.out.println("Current Region : "+region_current);

			JWTPayload jwtPayload = jweValidator.validateToken(jwtToken, region_current);
			String newEncryptedJWT = null;
			/*
			 * If token is valid and present in Redis then
			 * 1. remove existing key from redis.
			 * 2. create new token.
			 * 3. store new token in redis.
			 * */

			if(jwtPayload.isValid())
			{
				System.out.println("User found in different region : "+jwtPayload.isFoundInDiffRegion());
				jweGenerator.removeFromCache(jwtPayload.getRedisKey(), jwtPayload.isFoundInDiffRegion());

				UserInfo user = new UserInfo(jwtPayload.getUserName(), invokedFunctionArn);
				user.setRedisKey(user.getUserName()+":"+UUID.randomUUID().toString());
				newEncryptedJWT = jweGenerator.generateJWE(null, user);

				jweGenerator.addToCache(user);

			}else {
				throw new InvalidTokenException("Token not valid !!");
			}
			return newEncryptedJWT;

		}catch(IllegalAccessException | NoSuchAlgorithmException |InvalidKeySpecException |ParseException | IOException | JOSEException e) {
			e.printStackTrace();
			throw e;
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public ResponseEntity<String> logout(String jwtToken, Context context) {
		try {
			JWTPayload tokenPayload = jweValidator.extractTokenData(jwtToken);
			if (tokenPayload!=null && jweValidator.verifyTokenInfo(tokenPayload)) {
				String request_region = context.getInvokedFunctionArn().split(":")[3];
				System.out.println("Request region : "+request_region+", Token latest region : "+tokenPayload.getRegionLatest());
				if(jweGenerator.removeFromCache(tokenPayload.getRedisKey(), !request_region.equals(tokenPayload.getRegionLatest()))) {
					return ResponseEntity.ok().body("{'Message':'Logout Successful'}");
				}else {
					return ResponseEntity.ok().body("{'Message':'Logout Unsuccessful'}");
				}
			}else {
				return ResponseEntity.badRequest().body("{'Message':'Invalid Username or Expired Token'}");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("{'OOPS!!!  Error Occurred'}");
		}
	}


	@PostMapping("/user/register")
	public ResponseEntity<UserInfo> register(@RequestBody UserInfo user){
		if(user!=null) {
			String passwordHash = util.generateHash(user.getPassword());
			user.setPassword(passwordHash);
		}
		UserInfo savedUser = dynamoDbRepo.save(user);
		return ResponseEntity.status(200).body(savedUser);
	}

	@Value("${spring.application.name}")
	private String appName;

	@GetMapping("/hello")
	public String hello() {
		return "Hello from "+appName;
	}


}
package com.demo.token.authenticator.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.demo.token.dao.AccountInfo;
import com.demo.token.dao.Credentials;
import com.demo.token.dao.JWTPayload;
import com.demo.token.dao.Response;
import com.demo.token.dao.UserInfo;
import com.demo.token.dao.repo.DynamoDbRepo;
import com.demo.token.utils.RedisUtils;
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
	private RedisUtils redisUtils;

	@Autowired
	public AuthenticatorService(JWEGenerator jweGenerator, JWEValidator jweValidator, DynamoDbRepo dynamoDbRepo, CommonUtils util, Gson gson,RedisUtils redisUtils) {
		this.jweGenerator = jweGenerator;
		this.jweValidator = jweValidator;
		this.dynamoDbRepo = dynamoDbRepo;
		this.util = util;
		this.gson = gson;
		this.redisUtils = redisUtils;
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

					redisUtils.addToCache(user);

					HttpHeaders header = new HttpHeaders();
					header.add(HttpHeaders.AUTHORIZATION, encryptedJWT);
					header.add("Region", credentials.getArn().split(":")[3]);
					//header.add("AccountId", user.getAccountNumber());
					//header.add("AccountNumber", getLastNDigit(user.getAccountNumber(), 4));
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
					String sessionId = UUID.randomUUID().toString();
					user.setArn(context.getInvokedFunctionArn());
					user.setRedisKey(user.getUserName()+":"+sessionId);

					redisUtils.addToCache(user);

					user = generateAccountId(user);
					redisUtils.addAccountIdToCache(user, sessionId);

					String encryptedJWT = jweGenerator.generateJWE(null, user);

					System.out.println("ARN : "+user.getArn());

					Response response = generateResponse(user);

					Map<String, String> header = new HashMap<String,String>();
					header.put(HttpHeaders.AUTHORIZATION, encryptedJWT);
					header.put("Region", user.getArn().split(":")[3]);
					header.put("Access-Control-Allow-Origin", "*");
					header.put("Access-Control-Expose-Headers", "*");

					return new APIGatewayProxyResponse(200, header, gson.toJson(response), true);

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

	private Response generateResponse(UserInfo user) {

		Response response = new Response();
		response.setMessage("Login Successful");
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		if(user!=null && user.getAccount()!=null) {
			for(AccountInfo account : user.getAccount()) {
				Map<String, String> map = new HashMap<>();
				map.put("accountId", account.getAccountId());
				map.put("accountNumber", getLastNDigit(account.getAccountNumber(), 4));
				list.add(map);
			}
		}
		response.setAccount(list);
		return response;
	}


	private UserInfo generateAccountId(UserInfo user) {
		if(user!=null && user.getAccount()!=null) {
			System.out.println("AccountId -> AccountNumber");
			for(AccountInfo account : user.getAccount()) {
				account.setAccountId(UUID.randomUUID().toString());
				System.out.println(account.getAccountId()+" -> "+account.getAccountNumber());
			}
		}
		return user;
	}

	private String getLastNDigit(String number, int digits) {
		StringBuilder s = new StringBuilder(number); 
		IntStream.rangeClosed(0, number.length()-digits-1).forEach(n -> s.setCharAt(n, '*'));
		return s.toString();
	}

	//@RequestMapping(value = "/token/authorize", method = RequestMethod.POST)
	public String validate2(String jwtToken, String invokedFunctionArn) throws Exception {

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
				redisUtils.removeFromCache(jwtPayload.getRedisKey(), jwtPayload.isFoundInDiffRegion());

				UserInfo user = new UserInfo(jwtPayload.getUserName(), invokedFunctionArn, jwtPayload.getRedisKey());
				newEncryptedJWT = jweGenerator.generateJWE(null, user);

				redisUtils.addToCache(user);

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

	public Map<String, String> validateAdv(String jwtToken, String invokedFunctionArn, APIGatewayProxyRequest input)throws Exception {
		Map<String, String> validateResponseMap = new HashMap<String, String>();
		try {
			String region_current = invokedFunctionArn.split(":")[3];
			System.out.println("Current Region : "+region_current);

			JWTPayload jwtPayload = jweValidator.validateToken(jwtToken, region_current);
			String newEncryptedJWT = null;

			if(jwtPayload.isValid())
			{
				String accountId = input.getHeaders().get("AccountId");
				System.out.println("User found in different region : "+jwtPayload.isFoundInDiffRegion());
				redisUtils.removeFromCache(jwtPayload.getRedisKey(), jwtPayload.isFoundInDiffRegion());

				UserInfo user = new UserInfo(jwtPayload.getUserName(), invokedFunctionArn, jwtPayload.getRedisKey());
				newEncryptedJWT = jweGenerator.generateJWE(null, user);

				redisUtils.addToCache(user);

				String accountNumber = redisUtils.getAccount(jwtPayload.getRedisKey(), accountId);

				validateResponseMap.put("token", newEncryptedJWT);
				validateResponseMap.put("accountNumber", accountNumber);

			}else {
				throw new InvalidTokenException("Token not valid !!");
			}
			return validateResponseMap;

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
				if(redisUtils.removeFromCache(tokenPayload.getRedisKey(), !request_region.equals(tokenPayload.getRegionLatest()))) {
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
		UserInfo savedUser = null;
		try {
			if(user!=null) {
				System.out.println("User to be added : "+gson.toJson(user));
				String passwordHash = util.generateHash(user.getPassword());
				user.setPassword(passwordHash);
			}
			savedUser = dynamoDbRepo.save(user);
			return ResponseEntity.status(200).body(savedUser);
		}catch(Exception e) {
			System.out.println("Exception caught : "+e.getMessage());
			e.printStackTrace();
		}
		return ResponseEntity.status(400).body(savedUser);
	}

	@Value("${spring.application.name}")
	private String appName;

	@GetMapping("/hello")
	public String hello() {
		return "Hello from "+appName;
	}

}
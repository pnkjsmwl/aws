package com.demo.token.authenticator.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.lambda.runtime.Context;
import com.demo.token.authenticator.exception.InvalidTokenException;
import com.demo.token.dao.APIGatewayProxyRequest;
import com.demo.token.dao.APIGatewayProxyResponse;
import com.demo.token.dao.Credentials;
import com.demo.token.dao.JWTPayload;
import com.demo.token.dao.Response;
import com.demo.token.dao.UserInfo;
import com.demo.token.dao.repo.DynamoDbRepo;
import com.demo.token.utils.AuthUtils;
import com.demo.token.utils.CommonUtils;
import com.demo.token.utils.RedisUtils;
import com.google.gson.Gson;
import com.nimbusds.jose.JOSEException;

@RestController
public class AuthenticatorService  {
	//private Log log = LogFactory.getLog(AuthenticatorService.class);

	private JWEGenerator jweGenerator;
	private JWEValidator jweValidator;
	private DynamoDbRepo dynamoDbRepo;
	private AuthUtils util;
	private Gson gson;
	private RedisUtils redisUtils;
	private CommonUtils commonUtils;

	@Autowired
	public AuthenticatorService(JWEGenerator jweGenerator, JWEValidator jweValidator, DynamoDbRepo dynamoDbRepo, AuthUtils util, Gson gson,RedisUtils redisUtils, CommonUtils commonUtils) {
		this.jweGenerator = jweGenerator;
		this.jweValidator = jweValidator;
		this.dynamoDbRepo = dynamoDbRepo;
		this.util = util;
		this.gson = gson;
		this.redisUtils = redisUtils;
		this.commonUtils = commonUtils;
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
		Map<String, String> errorResp = new HashMap<>();
		try {
			Credentials cred = gson.fromJson(request.getBody(), Credentials.class);
			System.out.println("User from request : "+cred.getUserName());
			UserInfo user = dynamoDbRepo.getUserByUserName(cred.getUserName());
			if (user!=null) {
				if(util.validatePassword(cred.getPassword(), user.getPassword())) {
					String sessionId = UUID.randomUUID().toString();
					user.setArn(context.getInvokedFunctionArn());
					user.setRedisKey(user.getUserName()+":"+sessionId);

					/* Session cache */
					redisUtils.addToCache(user);

					user = commonUtils.generateAccountId(user);

					/* Account data cache */
					Map<String,String> map = new HashMap<>();
					if(user!=null && !user.getAccount().isEmpty()) {
						user.getAccount().forEach(account -> map.put(account.getAccountId(), account.getAccountNumber()));
					}
					redisUtils.addAccountIdToCache(map, sessionId);

					String encryptedJWT = jweGenerator.generateJWE(null, user);

					System.out.println("ARN : "+user.getArn());

					Response response = commonUtils.generateResponse(user);

					Map<String, String> header = new HashMap<String,String>();
					header.put(HttpHeaders.AUTHORIZATION, encryptedJWT);
					header.put("Region", user.getArn().split(":")[3]);
					header.put("Access-Control-Allow-Origin", "*");
					header.put("Access-Control-Expose-Headers", "*");

					return new APIGatewayProxyResponse(200, header, gson.toJson(response), true);

				} else {
					errorResp.put("Message", "Invalid Password");
					return new APIGatewayProxyResponse(403, null, gson.toJson(errorResp), true);
				}
			}else {
				errorResp.put("Message", "Invalid Username");
				return new APIGatewayProxyResponse(403, null, gson.toJson(errorResp), true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			errorResp.put("Message", "OOPS !!! Error Occurred");
			errorResp.put("Error", e.getMessage());
			return new APIGatewayProxyResponse(500, null, gson.toJson(errorResp), true);
		}
	}

	//@RequestMapping(value = "/token/authorize", method = RequestMethod.POST)
	public String validate2(String jwtToken, String invokedFunctionArn) throws Exception {

		try {
			String region_current = invokedFunctionArn.split(":")[3];
			System.out.println("Current Region : "+region_current);

			JWTPayload jwtPayload = jweValidator.validateToken(jwtToken, region_current, null, null);
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
			String accountId = input.getHeaders().get("AccountId")!= null? input.getHeaders().get("AccountId") : input.getHeaders().get("accountid");

			JWTPayload jwtPayload = jweValidator.validateToken(jwtToken, region_current, invokedFunctionArn, accountId);
			String newEncryptedJWT = null;

			if(jwtPayload.isValid()) {

				System.out.println("User found in different region : "+jwtPayload.isFoundInDiffRegion());

				// redisUtils.removeFromCache(jwtPayload.getRedisKey(), jwtPayload.isFoundInDiffRegion());

				UserInfo user = new UserInfo(jwtPayload.getUserName(), invokedFunctionArn, jwtPayload.getRedisKey());
				newEncryptedJWT = jweGenerator.generateJWE(null, user);

				/* Session entry */
				redisUtils.addToCache(user);

				validateResponseMap.put("token", newEncryptedJWT);

				if(accountId!=null) {
					String accountNumber = redisUtils.getAccount(jwtPayload.getRedisKey(), accountId);
					validateResponseMap.put("accountNumber", accountNumber);
				}

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

	public Response validateTokenDiffRegion(String token, String invokedFunctionArn, APIGatewayProxyRequest input) throws Exception {
		Response resp = new Response();
		try {
			String region_current = invokedFunctionArn.split(":")[3];
			System.out.println("Current Region : "+region_current);
			String accountId = input.getHeaders().get("AccountId")!= null? input.getHeaders().get("AccountId") : input.getHeaders().get("accountid");
			System.out.println("AccountId : "+accountId);
			JWTPayload jwtPayload = jweValidator.validateToken(token, region_current, invokedFunctionArn, accountId);
			
			if(jwtPayload.isValid())
			{
				Map<String, String> accountMap = redisUtils.getAccountMap(jwtPayload.getRedisKey(), accountId);
				System.out.println("Account Map from redis : "+accountMap);
				resp.setMessage("Valid Token");
				resp.setStatus("success");
				resp.setCode("0");
				resp.setMap(accountMap);
			}else {
				resp.setMessage("Invalid Token");
				resp.setStatus("fail");
				resp.setCode("1");
			}
			return resp;

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
		Response response = new Response();
		try {
			JWTPayload tokenPayload = jweValidator.extractTokenData(jwtToken);
			if (tokenPayload!=null && jweValidator.verifyTokenInfo(tokenPayload)) {
				String request_region = context.getInvokedFunctionArn().split(":")[3];
				System.out.println("Request region : "+request_region+", Token latest region : "+tokenPayload.getRegionLatest());

				if(redisUtils.removeFromCache(tokenPayload.getRedisKey(), !request_region.equals(tokenPayload.getRegionLatest()))) {
					response.setMessage("Logout Successful");
					response.setStatus("Success");
					return ResponseEntity.ok().body(gson.toJson(response));
				}else {
					response.setMessage("Logout Unsuccessful");
					response.setStatus("Fail");
					return ResponseEntity.ok().body(gson.toJson(response));
				}
			}else {
				response.setMessage("Invalid Username or Expired Token");
				response.setStatus("Fail");
				return ResponseEntity.badRequest().body(gson.toJson(response));
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setMessage("OOPS!!! Error Occurred");
			response.setStatus("Fail");
			response.setError(e.getMessage());
			return ResponseEntity.status(500).body(gson.toJson(response));
		}
	}

	public APIGatewayProxyResponse logout2(String jwtToken, Context context) {
		Response response = new Response();
		Map<String, String> header = new HashMap<String,String>();
		header.put("Access-Control-Allow-Origin", "*");
		header.put("Access-Control-Expose-Headers", "*");
		try {
			JWTPayload tokenPayload = jweValidator.extractTokenData(jwtToken);
			if (tokenPayload!=null && jweValidator.verifyTokenInfo(tokenPayload)) {
				String request_region = context.getInvokedFunctionArn().split(":")[3];
				System.out.println("Request region : "+request_region+", Token latest region : "+tokenPayload.getRegionLatest());

				if(redisUtils.removeFromCache(tokenPayload.getRedisKey(), !request_region.equals(tokenPayload.getRegionLatest()))) {
					response.setMessage("Logout Successful");
					response.setStatus("Success");
					return new APIGatewayProxyResponse(200, header, gson.toJson(response), true);
				}else {
					response.setMessage("Logout Unsuccessful");
					response.setStatus("Fail");
					return new APIGatewayProxyResponse(403, header, gson.toJson(response), true);
				}
			}else {
				response.setMessage("Invalid Username or Expired Token");
				response.setStatus("Fail");
				return new APIGatewayProxyResponse(403, header, gson.toJson(response), true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setMessage("OOPS!!! Error Occurred");
			response.setStatus("Fail");
			response.setError(e.getMessage());
			return new APIGatewayProxyResponse(500, header, gson.toJson(response), true);
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

}
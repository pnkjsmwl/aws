package com.demo.token.authenticator.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
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

import com.demo.token.authenticator.utils.CommonUtils;
import com.demo.token.dao.APIGatewayProxyResponse;
import com.demo.token.dao.Credentials;
import com.demo.token.dao.JWTPayload;
import com.demo.token.dao.UserInfo;
import com.demo.token.dao.repo.DynamoDbRepo;
import com.nimbusds.jose.JOSEException;

@RestController
public class AuthenticatorService  {
	//private Log log = LogFactory.getLog(AuthenticatorService.class);

	private JWEGenerator jweGenerator;
	private JWEValidator jweValidator;
	private DynamoDbRepo dynamoDbRepo;
	private CommonUtils util;

	@Autowired
	public AuthenticatorService(JWEGenerator jweGenerator, JWEValidator jweValidator, DynamoDbRepo dynamoDbRepo, CommonUtils util) {
		this.jweGenerator = jweGenerator;
		this.jweValidator = jweValidator;
		this.dynamoDbRepo = dynamoDbRepo;
		this.util = util;
	}

	@RequestMapping(value = "/signon", method = RequestMethod.POST)
	public ResponseEntity<String> signon(@RequestBody Credentials credentials) {
		try {
			UserInfo user = dynamoDbRepo.getUserByUserName(credentials.getUsername());
			if (user!=null) {
				if(util.validatePassword(credentials.getPassword(), user.getPassword())){
					user.setArn(credentials.getArn());
					String encryptedJWT = jweGenerator.generateJWE(null, user);
					jweGenerator.cacheSignon(user);
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
	public APIGatewayProxyResponse signon2(@RequestBody Credentials credentials) {
		Map<String, String> header2 = new HashMap<String,String>();
		header2.put(HttpHeaders.AUTHORIZATION, "dummy value");
		
		try {
			UserInfo user = dynamoDbRepo.getUserByUserName(credentials.getUsername());
			
			if (user!=null) {
				if(util.validatePassword(credentials.getPassword(), user.getPassword())){
					user.setArn(credentials.getArn());
					String encryptedJWT = jweGenerator.generateJWE(null, user);
					jweGenerator.cacheSignon(user);

					Map<String, String> header = new HashMap<String,String>();
					header.put(HttpHeaders.AUTHORIZATION, encryptedJWT);
					header.put("Region", credentials.getArn().split(":")[3]);
					header.put("AccountId", user.getAccountNumber());
					header.put("AccountNumber", getLastNDigit(user.getAccountNumber(), 4));

					return new APIGatewayProxyResponse(200, header, "Message : Login Successful", true);

				} else {
					return new APIGatewayProxyResponse(403, header2, "Error : Invalid Password", true);
				}
			}else {
				return new APIGatewayProxyResponse(403, header2, "Error : Invalid Username", true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new APIGatewayProxyResponse(500, header2, "OOPS !!! Error Occurred", true);
		}
	}

	private String getLastNDigit(String number, int digits) {
		StringBuilder s = new StringBuilder(number); 
		IntStream.rangeClosed(0, number.length()-digits-1).forEach(n -> s.setCharAt(n, '*'));
		return s.toString();
	}

	//@RequestMapping(value = "/token/authorize", method = RequestMethod.POST)
	public boolean validate(String jwtToken) {
		try {
			return jweValidator.validateToken(jwtToken);

		}catch(IllegalAccessException | NoSuchAlgorithmException |InvalidKeySpecException |ParseException | IOException | JOSEException e) {
			e.printStackTrace();
			return false;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public ResponseEntity<String> logout(String jwtToken) {
		try {
			JWTPayload tokenPayload = jweValidator.extractTokenData(jwtToken);
			if (tokenPayload!=null && jweValidator.verifyTokenInfo(tokenPayload)) {

				if(jweGenerator.logoutUser(tokenPayload.getUserName())) {
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
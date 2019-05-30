package com.demo.token.authenticator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.demo.token.authenticator.utils.CommonUtils;
import com.demo.token.dao.Caller;
import com.demo.token.dao.Credentials;
import com.demo.token.dao.UserInfo;
import com.demo.token.dao.repo.DynamoDbRepo;

@RestController
public class AuthenticatorService  {

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
			UserInfo user = new UserInfo("1982713", "pankaj", "test123", "121231231231", "user");//userInfoRepo.findByUserName(credentials.getUserName());
			/*if (user!=null) {*/
			//if (util.validatePassword(credentials.getPassword(), user.getPassword())) {
			if("test123".equals(credentials.getPassword())){
				user.setArn(credentials.getArn());
				String encryptedJWT = jweGenerator.generateJWE(null, user);
				jweGenerator.cacheToken(encryptedJWT);
				HttpHeaders headers = new HttpHeaders();
				headers.add(HttpHeaders.AUTHORIZATION, encryptedJWT);
				return ResponseEntity.ok().headers(headers).body("{'Message':'Login Successful'}");

			} else {
				return ResponseEntity.badRequest().body("{'Error':'Invalid Password'}");
			}
			/* }else {
				return ResponseEntity.badRequest().body("{'Error':'Invalid Username'}");
			}*/
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("{'OOPS!!!  Error Occurred'}");
		}
	}


	@RequestMapping(value = "/token/authorize", method = RequestMethod.POST)
	public ResponseEntity<?> validate(@RequestHeader("Authorization") String jwtToken, @RequestBody Caller caller) {
		try{
			return ResponseEntity.ok().body(jweValidator.validateToken(jwtToken, caller));
		}catch(IllegalAccessException e){
			e.printStackTrace();
			return ResponseEntity.status(403).body("Unauthorized access");
		}
		catch(Exception e){
			e.printStackTrace();
			return ResponseEntity.status(500).body("Server side error");
		}
	}

	@PostMapping("/user/add")
	public ResponseEntity<UserInfo> add(@RequestBody UserInfo user){
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
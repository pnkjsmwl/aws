package com.demo.token.authenticator.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import redis.clients.jedis.Jedis;

@RestController
public class AuthenticatorService  {
	private Log log = LogFactory.getLog(AuthenticatorService.class);

	private JWEGenerator jweGenerator;
	private JWEValidator jweValidator;
	private DynamoDbRepo dynamoDbRepo;
	private CommonUtils util;
	private Jedis jedis;

	@Autowired
	public AuthenticatorService(JWEGenerator jweGenerator, JWEValidator jweValidator, DynamoDbRepo dynamoDbRepo, CommonUtils util, Jedis jedis) {
		this.jweGenerator = jweGenerator;
		this.jweValidator = jweValidator;
		this.dynamoDbRepo = dynamoDbRepo;
		this.util = util;
		this.jedis = jedis;
	}

	@RequestMapping(value = "/signon", method = RequestMethod.POST)
	public ResponseEntity<String> signon(@RequestBody Credentials credentials) {
		try {
			UserInfo user = dynamoDbRepo.getUserByUserName(credentials.getUsername());
			if (user!=null) {
				if(util.validatePassword(credentials.getPassword(), user.getPassword())){
					user.setArn(credentials.getArn());
					String encryptedJWT = jweGenerator.generateJWE(null, user);
					jweGenerator.cacheToken(encryptedJWT);
					HttpHeaders headers = new HttpHeaders();
					headers.add(HttpHeaders.AUTHORIZATION, encryptedJWT);
					return ResponseEntity.ok().headers(headers).body("{'Message':'Login Successful'}");

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

	@RequestMapping(value = "/token/authorize", method = RequestMethod.POST)
	public ResponseEntity<?> validate(@RequestHeader("Authorization") String jwtToken, @RequestBody Caller caller) {
		try{
			String tokenFromRedis = jedis.get(caller.getAuthorizationToken());
			log.info("Value from redis against token : "+tokenFromRedis);
			System.out.println("Value from redis against token : "+tokenFromRedis);
			if(tokenFromRedis!=null)
			{
				return ResponseEntity.ok().body(jweValidator.validateToken(caller.getAuthorizationToken(), caller));
			}
		}catch(IllegalAccessException e){
			e.printStackTrace();
			return ResponseEntity.status(403).body("Unauthorized access");
		}
		catch(Exception e){
			e.printStackTrace();
			return ResponseEntity.status(500).body("Server side error");
		}
		return ResponseEntity.status(403).body("Unauthorized access");
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
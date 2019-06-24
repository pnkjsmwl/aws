package com.bhavani.authenticator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.bhavani.authenticator.dao.Caller;
import com.bhavani.authenticator.dao.Credentials;
import com.bhavani.authenticator.dao.UserInfo;
import com.bhavani.authenticator.repo.UserInfoRepo;
import com.bhavani.authenticator.utils.CommonUtils;

@RestController
public class AuthenticatorService {
	/*
	 * @Autowired
	 * 
	 * @Qualifier(value = "userData") private HashMap<String, UserInfo> userdata;
	 */

	@Autowired
	private JWEGenerator jweGenerator;

	@Autowired
	private JWEValidator jweValidator;

	@Autowired
	private UserInfoRepo userInfoRepo;

	@Autowired
	public CommonUtils util;

	@RequestMapping(value = "/signon", method = RequestMethod.POST)
	public ResponseEntity<String> signon(@RequestBody Credentials credentials) {
		try {
			UserInfo user = userInfoRepo.findByUserName(credentials.getUserName());
			if (user!=null) {
				if (util.validatePassword(credentials.getPassword(), user.getPassword())) {
					String encryptedJWT = jweGenerator.generateJWE(null, user);
					jweGenerator.cacheToken(encryptedJWT);
					HttpHeaders headers = new HttpHeaders();
					headers.add(HttpHeaders.AUTHORIZATION, encryptedJWT);
					return ResponseEntity.ok().headers(headers).body("{'Message':'Login Successful'}");

				} else {
					return ResponseEntity.badRequest().body("{'Error':'Invalid Password'}");
				}
			} else {
				return ResponseEntity.badRequest().body("{'Error':'Invalid Username'}");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("{'OOPS!!!  Error Occurred'}");
		}

	}


	@RequestMapping(value = "/token/authorize", method = RequestMethod.POST)
	public ResponseEntity<?> signon(@RequestHeader("Authorization") String jwtToken, @RequestBody Caller caller) {
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
		UserInfo savedUser = userInfoRepo.save(user);
		return ResponseEntity.status(200).body(savedUser);
	}
	
}
package com.bhavani.authenticator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.bhavani.authenticator.dao.UserInfo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AuthenticatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthenticatorApplication.class, args);
	}

	@Bean(name = "userData")
	public HashMap<String, UserInfo> userData() {
		HashMap<String, UserInfo> userData = new HashMap<>();
		UserInfo u1 = new UserInfo();
		u1.setUserName("Bhavani");
		u1.setPassword("test123");
		u1.setAccountNumber("1234");
		u1.setRole("ACCOUNTS_ROLE");

		UserInfo u2 = new UserInfo();
		u2.setUserName("Prasad");
		u2.setPassword("test567");
		u2.setAccountNumber("5678");
		u2.setRole("PROFILE_ROLE");

		userData.put(u1.getUserName(), u1);
		userData.put(u2.getUserName(), u2);

		return userData;
	}

	@Bean(name = "policy")
	HashMap<String, List<String>> getPolicy() {
		HashMap<String,List<String>> policy = new HashMap<>();
		policy.put("GET/account/summary",Arrays.asList("ACCOUNTS_ROLE","ADMIN"));
		policy.put("GET/profile/email",Arrays.asList("PROFILE_ROLE","ADMIN"));
		policy.put("GET/profile/mobile",Arrays.asList("PROFILE_ROLE","ADMIN"));
		policy.put("GET/profile/address",Arrays.asList("PROFILE_ROLE","ADMIN"));
		return policy;
	}
}

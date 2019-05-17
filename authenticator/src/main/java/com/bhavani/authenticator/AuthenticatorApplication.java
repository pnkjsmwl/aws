package com.bhavani.authenticator;

import java.util.HashMap;

import com.bhavani.authenticator.dao.UserInfo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AuthenticatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthenticatorApplication.class, args);
	}

	@Bean(name="userData")
	public HashMap<String,UserInfo> userData(){
		HashMap<String,UserInfo> userData = new HashMap<>();
		UserInfo u1 = new UserInfo();
		u1.setUserName("Bhavani");
		u1.setPassword("test123");
		u1.setAccountNumber("1234");

		UserInfo u2 = new UserInfo();
		u2.setUserName("Prasad");
		u2.setPassword("test567");
		u2.setAccountNumber("5678");

		userData.put(u1.getUserName(), u1);
		userData.put(u2.getUserName(), u2);

		return userData;
	}

}

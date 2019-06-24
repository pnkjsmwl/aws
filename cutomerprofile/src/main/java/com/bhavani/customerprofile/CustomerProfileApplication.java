package com.bhavani.cutomerprofile;

import java.util.HashMap;

import com.bhavani.cutomerprofile.dao.CustomerProfile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CustomerProfileApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomerProfileApplication.class, args);
	}

	@Bean(name="profileData")
	HashMap<String,CustomerProfile> getProfileData() {
		HashMap<String,CustomerProfile> data = new HashMap<>();
		CustomerProfile p1 = new CustomerProfile();
		p1.setEmail("prasad@hotmail.com");
		p1.setMobile("+197261234567");
		p1.setAddress("Dallas, Texas");

		CustomerProfile p2 = new CustomerProfile();
		p2.setEmail("bhavani@hotmail.com");
		p2.setMobile("+124861234567");
		p2.setAddress("Detroit, Denver");

		data.put("Bhavani",p2);
		data.put("Prasad",p1);

		return data;
	}
}

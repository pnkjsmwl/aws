package com.demo.customerprofile.config;

import java.util.HashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.demo.cutomerprofile.doc.CustomerProfile;

@Configuration
public class ApplConfig {

	@Bean(name="profileData")
	HashMap<String,CustomerProfile> getProfileData() {
		HashMap<String,CustomerProfile> data = new HashMap<>();
		CustomerProfile p1 = new CustomerProfile();
		p1.setEmail("ganesh@hotmail.com");
		p1.setMobile("+197261234567");
		p1.setAddress("Dallas, Texas");

		CustomerProfile p2 = new CustomerProfile();
		p2.setEmail("bhavani@hotmail.com");
		p2.setMobile("+124861234567");
		p2.setAddress("Detroit, Denver");

		CustomerProfile p3 = new CustomerProfile();
		p3.setEmail("pankaj@hotmail.com");
		p3.setMobile("+124861234567");
		p3.setAddress("Irving, Texas");

		data.put("Ganesh",p1);
		data.put("Bhavani",p2);
		data.put("Pankaj",p3);

		return data;
	}
}

package com.demo.cutomerprofile.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.cutomerprofile.doc.CustomerProfile;

@RestController
@RequestMapping("/customer-crud")
public class CustomerProfileCrudController{
	private Log log = LogFactory.getLog(CustomerProfileCrudController.class);

	@Autowired
	@Qualifier("profileData")
	HashMap<String,CustomerProfile> profileData;

	@GetMapping("/summary")
	public ResponseEntity<?> getSUmmary(@RequestParam String userName){
		log.info("Inside get summary !!!");
		Map<String,String> map = new HashMap<String,String>();
		map.put("Email", profileData.get(userName).getEmail());
		map.put("Mobile", profileData.get(userName).getMobile());
		map.put("Address", profileData.get(userName).getAddress());
		return ResponseEntity.ok().body(map);
	}
}
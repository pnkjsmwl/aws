package com.demo.customerprofile.probe;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileCrudProbeController {

	@GetMapping("/live")
	public ResponseEntity<?> liveCheck(){
		Map<String,String> map = new HashMap<>();
		map.put("Message", "Profile Crud App is live.");
		return ResponseEntity.status(200).body(map);
	}

	@GetMapping("/ready")
	public ResponseEntity<?> readyCheck(){

		/*
		 * Here we need to implement some kind of logic which determines whether app is ready to server traffic, logic could include
		 * 
		 * 1. db connection check.
		 * 2. if app is dependent on any FS then check if its available.
		 * 
		 * */
		Map<String,String> map = new HashMap<>();
		map.put("Message", "Profile Crud App is ready.");

		return ResponseEntity.status(200).body(map);
	}
}

package com.demo.customerprofile.probe;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class ProfileProbeController {
	private Log log = LogFactory.getLog(ProfileProbeController.class);

	@Value("${customer_crud.url}")
	private String customerCrudUrl;

	@Autowired
	private RestTemplate accountRestTemplate;

	@GetMapping("/live")
	public ResponseEntity<?> liveCheck(){
		Map<String,String> map = new HashMap<>();
		map.put("Message", "Profile App is live.");
		return ResponseEntity.status(200).body(map);
	}

	@SuppressWarnings("rawtypes")
	@GetMapping("/ready")
	public ResponseEntity<?> readyCheck(){

		/*
		 * Here we need to implement some kind of logic which determines whether app is ready to server traffic, logic could include
		 * 
		 * 1. db connection check.
		 * 2. if app is dependent on any FS then check if its available.
		 * 
		 * */

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("userName", "Pankaj");

		String url = customerCrudUrl+"/customer-crud/summary";
		log.info(url);
		URI uri = UriComponentsBuilder.fromUriString(url)
				.queryParams(params)
				.build()
				.toUri();

		ResponseEntity<HashMap> respEntity = accountRestTemplate.exchange(uri, HttpMethod.GET, requestEntity, HashMap.class);
		Map<String,String> map = new HashMap<>();
		if(respEntity.getStatusCode()==HttpStatus.OK) {
			map.put("Message", "Profile App is ready.");
			return ResponseEntity.status(200).body(map);
		}
		map.put("Message", "Profile App is unavailable.");
		return ResponseEntity.status(503).body(map);
	}

}

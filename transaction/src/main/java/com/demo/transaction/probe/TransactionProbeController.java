package com.demo.transaction.probe;

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

import com.demo.transaction.doc.Transaction;

@RestController
public class TransactionProbeController {
	private Log log = LogFactory.getLog(TransactionProbeController.class);

	@Value("${transaction_crud.url}")
	private String transactionCrudUrl;

	@Autowired
	private RestTemplate accountRestTemplate;

	@GetMapping("/live")
	public ResponseEntity<?> liveCheck(){
		Map<String,String> map = new HashMap<>();
		map.put("Message", "Transaction App is live.");
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

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("accountNumber", "1234567812345678");

		String url = transactionCrudUrl+"/transaction-crud/last";
		log.info(url);
		URI uri = UriComponentsBuilder.fromUriString(url)
				.queryParams(params)
				.build()
				.toUri();

		ResponseEntity<Transaction> respEntity = accountRestTemplate.exchange(uri, HttpMethod.GET, requestEntity, Transaction.class);
		Map<String,String> map = new HashMap<>();

		if(respEntity.getStatusCode()==HttpStatus.OK) {
			map.put("Message", "Transaction App is ready.");
			return ResponseEntity.status(200).body(map);
		}
		map.put("Message", "Transaction App is unavailable.");
		return ResponseEntity.status(503).body(map);
	}

}

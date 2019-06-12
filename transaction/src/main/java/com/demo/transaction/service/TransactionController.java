package com.demo.transaction.service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.demo.transaction.doc.Transaction;
import com.demo.transaction.utils.RedisUtils;

@RestController
@RequestMapping("/transaction")
public class TransactionController {
	private Log log = LogFactory.getLog(TransactionController.class);

	@Value("${transaction_crud.url}")
	private String transactionCrudUrl;

	@Autowired
	private RestTemplate transactionRestTemplate;

	@Autowired
	private RedisUtils redisUtils;

	@GetMapping("/summary")
	public ResponseEntity<?> getTrasnactionSummary(@RequestParam String accountNumber, HttpServletRequest request) {

		if(accountNumber!=null) {

			String key = accountNumber+":"+request.getRequestURI();

			Transaction transactionFromRedis = (Transaction) redisUtils.getTransactionValue(key);
			if(transactionFromRedis!=null)
			{
				log.info("AccountNumber from Redis : "+transactionFromRedis.getAccountNumber());
				return ResponseEntity.ok(transactionFromRedis);
			}

			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);

			MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			params.add("accountNumber", accountNumber);

			HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

			String url = transactionCrudUrl+"/transaction-crud/summary";
			log.info(url);
			URI uri = UriComponentsBuilder.fromUriString(url)
					.queryParams(params)
					.build()
					.toUri();

			ResponseEntity<Transaction> respEntity = transactionRestTemplate.exchange(uri, HttpMethod.GET, requestEntity, Transaction.class);

			if(respEntity.getStatusCode()==HttpStatus.OK) {

				Transaction transaction = respEntity.getBody();

				redisUtils.setValue(key, transaction);

				return ResponseEntity.ok(transaction);
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	@GetMapping("/recent")
	public ResponseEntity<?> getRecentNTransactions(@RequestParam String accountNumber, @RequestParam String count, HttpServletRequest request) {

		if(accountNumber!=null) {

			String key = accountNumber+":"+request.getRequestURI()+":"+count;

			Map<?,?> mapValue = (Map<?, ?>) redisUtils.getMapValue(key);
			if(mapValue!=null)
			{
				log.info("Data from Redis : "+mapValue);
				return ResponseEntity.ok(mapValue);
			}

			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);

			MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			params.add("accountNumber", accountNumber);
			params.add("count", count);

			HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

			String url = transactionCrudUrl+"/transaction-crud/recent";
			log.info(url);
			URI uri = UriComponentsBuilder.fromUriString(url)
					.queryParams(params)
					.build()
					.toUri();

			ResponseEntity<HashMap> respEntity = transactionRestTemplate.exchange(uri, HttpMethod.GET, requestEntity, HashMap.class);

			if(respEntity.getStatusCode()==HttpStatus.OK) {
				HashMap hashMap = respEntity.getBody();

				redisUtils.setValue(key, hashMap);

				return ResponseEntity.ok(hashMap);
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	@GetMapping("/last")
	public ResponseEntity<?> getLastTransaction(@RequestParam String accountNumber, HttpServletRequest request) {

		if(accountNumber!=null) {

			String key = accountNumber+":"+request.getRequestURI();

			Map<?,?> mapValue = (Map<?, ?>) redisUtils.getMapValue(key);
			if(mapValue!=null)
			{
				log.info("Data from Redis : "+mapValue);
				return ResponseEntity.ok(mapValue);
			}

			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);

			MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			params.add("accountNumber", accountNumber);

			HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

			String url = transactionCrudUrl+"/transaction-crud/last";
			log.info(url);
			URI uri = UriComponentsBuilder.fromUriString(url)
					.queryParams(params)
					.build()
					.toUri();

			ResponseEntity<HashMap> respEntity = transactionRestTemplate.exchange(uri, HttpMethod.GET, requestEntity, HashMap.class);

			if(respEntity.getStatusCode()==HttpStatus.OK) {
				HashMap hashMap = respEntity.getBody();

				redisUtils.setValue(key, hashMap);

				return ResponseEntity.ok(hashMap);
			}
		}
		return null;
	}
}

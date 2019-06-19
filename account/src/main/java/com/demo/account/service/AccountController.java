package com.demo.account.service;

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

import com.demo.account.doc.Account;
import com.demo.account.utils.RedisUtils;

@RestController
@RequestMapping("/account")
public class AccountController {
	private Log log = LogFactory.getLog(AccountController.class);

	@Value("${account_crud.url}")
	private String accountCrudUrl;

	@Autowired
	private RestTemplate accountRestTemplate;

	@Autowired
	private RedisUtils redisUtils;


	@GetMapping("/summary")
	public ResponseEntity<?> getAccountSummary(@RequestParam String accountNumber, HttpServletRequest request) throws Exception {

		if(accountNumber!=null) {
			String key = accountNumber+":"+request.getRequestURI();

			Account accountFromRedis = (Account) redisUtils.getAccountValue(key);
			if(accountFromRedis!=null)
			{
				log.info("AccountNumber from Redis : "+accountFromRedis.getAccountNumber());
				/*
				 * HttpHeaders headers= new HttpHeaders();
				 * headers.add(HttpHeaders.AUTHORIZATION, encryptedJWT );
				 */

				return ResponseEntity.ok().body(accountFromRedis);
			}

			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);

			MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			params.add("accountNumber", accountNumber);

			HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

			String url = accountCrudUrl+"/account-crud/summary";
			log.info(url);
			URI uri = UriComponentsBuilder.fromUriString(url)
					.queryParams(params)
					.build()
					.toUri();

			ResponseEntity<Account> respEntity = accountRestTemplate.exchange(uri, HttpMethod.GET, requestEntity, Account.class);
			if(respEntity.getStatusCode()==HttpStatus.OK) {

				Account account = respEntity.getBody();

				redisUtils.setValue(key, account);

				/*
				 * HttpHeaders headers= new HttpHeaders();
				 * headers.add(HttpHeaders.AUTHORIZATION, encryptedJWT);
				 */

				/*
				 * HttpHeaders headers = new HttpHeaders();
				 * headers.add(HttpHeaders.AUTHORIZATION, "dummy value");
				 */return ResponseEntity.ok().body(account);
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	@GetMapping("/current-balance")
	public ResponseEntity<?> getCurrentBalance(@RequestParam String accountNumber,  HttpServletRequest request) throws Exception {
		if(accountNumber!=null) {
			String key = accountNumber+":"+request.getRequestURI();

			Map<?,?> mapValue = (Map<?, ?>) redisUtils.getMapValue(key);
			if(mapValue!=null) {
				log.info("Due data from Redis : "+mapValue);
				return ResponseEntity.ok().body(mapValue);
			}


			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);

			MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			params.add("accountNumber", accountNumber);

			HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

			String url = accountCrudUrl+"/account-crud/current-balance";
			log.info(url);
			URI uri = UriComponentsBuilder.fromUriString(url)
					.queryParams(params)
					.build()
					.toUri();

			ResponseEntity<HashMap> respEntity = accountRestTemplate.exchange(uri, HttpMethod.GET, requestEntity, HashMap.class);

			if(respEntity.getStatusCode()==HttpStatus.OK) {

				HashMap hashMap = respEntity.getBody();

				redisUtils.setValue(key, hashMap);

				return ResponseEntity.ok(hashMap);
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	@GetMapping("/due")
	public ResponseEntity<?> getDue(@RequestParam String accountNumber, HttpServletRequest request) {
		String key = accountNumber+":"+request.getRequestURI();

		if(accountNumber!=null) {

			Map<?,?> mapValue = (Map<?, ?>) redisUtils.getMapValue(key);
			if(mapValue!=null) {
				log.info("Due data from Redis : "+mapValue);
				return ResponseEntity.ok().body(mapValue);
			}

			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);

			MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			params.add("accountNumber", accountNumber);

			HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

			String url = accountCrudUrl+"/account-crud/due";
			log.info(url);
			URI uri = UriComponentsBuilder.fromUriString(url)
					.queryParams(params)
					.build()
					.toUri();

			ResponseEntity<HashMap> respEntity = accountRestTemplate.exchange(uri, HttpMethod.GET, requestEntity, HashMap.class);

			if(respEntity.getStatusCode()==HttpStatus.OK) {

				HashMap resp = respEntity.getBody();

				redisUtils.setValue(key, resp);

				return ResponseEntity.ok(resp);
			}
		}
		return null;
	}
}

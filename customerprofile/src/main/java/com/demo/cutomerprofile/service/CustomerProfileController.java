package com.demo.cutomerprofile.service;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.demo.cutomerprofile.doc.JWTPayload;
import com.demo.cutomerprofile.utils.RedisUtils;

@RestController
@RequestMapping("/customer")
public class CustomerProfileController{
	private Log log = LogFactory.getLog(CustomerProfileController.class);

	@Value("${customer_crud.url}")
	private String customerCrudUrl;

	@Value("${spring.application.name}")
	private String appName;

	@Value("${REDIS_HOST:localhost}")
	private String redis_host;

	@Autowired
	private RestTemplate accountRestTemplate;

	@Autowired
	private RedisUtils redisUtils;

	@Value("${message1}")
	private String message1;

	@SuppressWarnings("rawtypes")
	@GetMapping("/summary")
	public ResponseEntity<?> getSummary(HttpServletRequest request){
		log.info("Inside get email !!!");
		JWTPayload jwtPayload = (JWTPayload) request.getAttribute("JWTPayload");

		if(jwtPayload!=null) {

			String key = jwtPayload.getAccountNumber()+":"+request.getRequestURI();

			Map<?,?> mapValue = (Map<?, ?>)  redisUtils.getMapValue(key);
			if(mapValue!=null)
			{
				log.info("Summary from Redis : "+mapValue);
				/*
				 * HttpHeaders headers= new HttpHeaders();
				 * headers.add(HttpHeaders.AUTHORIZATION, encryptedJWT );
				 */
				return ResponseEntity.ok().body(mapValue);
			}

			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);

			MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			params.add("userName", jwtPayload.getUserName());

			HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

			String url = customerCrudUrl+"/customer-crud/summary";
			log.info(url);
			URI uri = UriComponentsBuilder.fromUriString(url)
					.queryParams(params)
					.build()
					.toUri();

			ResponseEntity<HashMap> respEntity = accountRestTemplate.exchange(uri, HttpMethod.GET, requestEntity, HashMap.class);
			if(respEntity.getStatusCode()==HttpStatus.OK) {

				HashMap resp = respEntity.getBody();

				redisUtils.setValue(key, resp);

				/*
				 * HttpHeaders headers= new HttpHeaders();
				 * headers.add(HttpHeaders.AUTHORIZATION, encryptedJWT);
				 */
				return ResponseEntity.ok(resp);
			}
		}
		return null;
	}

	@GetMapping("/email")
	public ResponseEntity<String> getEmail(HttpServletRequest request){
		log.info("Inside get email !!!");
		JWTPayload jwtPayload = (JWTPayload) request.getAttribute("JWTPayload");

		if(jwtPayload!=null) {

			String key = jwtPayload.getAccountNumber()+":"+request.getRequestURI();

			String emailFromRedis = (String) redisUtils.getStringValue(key);
			if(emailFromRedis!=null)
			{
				log.info("Email from Redis : "+emailFromRedis);
				/*
				 * HttpHeaders headers= new HttpHeaders();
				 * headers.add(HttpHeaders.AUTHORIZATION, encryptedJWT );
				 */
				return ResponseEntity.ok().body(String.format("{\"Email\": \"%s\"}",emailFromRedis));
			}

			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);

			MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			params.add("userName", jwtPayload.getUserName());

			HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

			String url = customerCrudUrl+"/customer-crud/email";
			log.info(url);
			URI uri = UriComponentsBuilder.fromUriString(url)
					.queryParams(params)
					.build()
					.toUri();

			ResponseEntity<String> respEntity = accountRestTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class);
			if(respEntity.getStatusCode()==HttpStatus.OK) {

				String email = respEntity.getBody();

				redisUtils.setValue(key, email);

				/*
				 * HttpHeaders headers= new HttpHeaders();
				 * headers.add(HttpHeaders.AUTHORIZATION, encryptedJWT);
				 */

				return ResponseEntity.ok().body(email);
			}
		}
		return null;
	}

	@GetMapping("/mobile")
	public ResponseEntity<String> getMobile(HttpServletRequest request){
		log.info("Inside get mobile !!!");
		JWTPayload jwtPayload = (JWTPayload) request.getAttribute("JWTPayload");

		if(jwtPayload!=null) {

			String key = jwtPayload.getAccountNumber()+":"+request.getRequestURI();

			String mobileFromRedis = (String) redisUtils.getStringValue(key);
			if(mobileFromRedis!=null)
			{
				log.info("Mobile from Redis : "+mobileFromRedis);
				/*
				 * HttpHeaders headers= new HttpHeaders();
				 * headers.add(HttpHeaders.AUTHORIZATION, encryptedJWT );
				 */
				return ResponseEntity.ok().body(String.format("{\"Mobile\": \"%s\"}",mobileFromRedis));
			}

			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);

			MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			params.add("userName", jwtPayload.getUserName());

			HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

			String url = customerCrudUrl+"/customer-crud/mobile";
			log.info(url);
			URI uri = UriComponentsBuilder.fromUriString(url)
					.queryParams(params)
					.build()
					.toUri();

			ResponseEntity<String> respEntity = accountRestTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class);
			if(respEntity.getStatusCode()==HttpStatus.OK) {

				String mobile = respEntity.getBody();

				redisUtils.setValue(key, mobile);

				/*
				 * HttpHeaders headers= new HttpHeaders();
				 * headers.add(HttpHeaders.AUTHORIZATION, encryptedJWT);
				 */

				return ResponseEntity.ok().body(mobile);
			}
		}
		return null;
	}

	@GetMapping("/address")
	public ResponseEntity<String> getAddress(HttpServletRequest request){
		log.info("Inside get address !!!");
		JWTPayload jwtPayload = (JWTPayload) request.getAttribute("JWTPayload");
		if(jwtPayload!=null) {

			String key = jwtPayload.getAccountNumber()+":"+request.getRequestURI();

			String addressFromRedis = (String) redisUtils.getStringValue(key);
			if(addressFromRedis!=null)
			{
				log.info("Mobile from Redis : "+addressFromRedis);
				/*
				 * HttpHeaders headers= new HttpHeaders();
				 * headers.add(HttpHeaders.AUTHORIZATION, encryptedJWT );
				 */
				return ResponseEntity.ok().body(String.format("{\"Address\": \"%s\"}",addressFromRedis));
			}

			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);

			MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			params.add("userName", jwtPayload.getUserName());

			HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

			String url = customerCrudUrl+"/customer-crud/address";
			log.info(url);
			URI uri = UriComponentsBuilder.fromUriString(url)
					.queryParams(params)
					.build()
					.toUri();

			ResponseEntity<String> respEntity = accountRestTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class);
			if(respEntity.getStatusCode()==HttpStatus.OK) {

				String address = respEntity.getBody();

				redisUtils.setValue(key, address);

				/*
				 * HttpHeaders headers= new HttpHeaders();
				 * headers.add(HttpHeaders.AUTHORIZATION, encryptedJWT);
				 */

				return ResponseEntity.ok().body(address);
			}
		}
		return null;
	}

	@GetMapping("/hello")
	public String hello() {
		log.info("Hello from :"+appName+", "+message1+" redis host : "+redis_host);
		return "Hello from :"+appName+", "+message1+" redis host : "+redis_host;
	}


}
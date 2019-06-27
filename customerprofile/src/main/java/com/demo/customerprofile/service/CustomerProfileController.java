package com.demo.customerprofile.service;

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

import com.demo.customerprofile.doc.JWTPayload;
import com.demo.customerprofile.utils.RedisUtils;

@RestController
@RequestMapping("/customer")
public class CustomerProfileController{
	private Log log = LogFactory.getLog(CustomerProfileController.class);

	@Value("${customer_crud.url}")
	private String customerCrudUrl;

	@Autowired
	private RestTemplate accountRestTemplate;

	@Autowired
	private RedisUtils redisUtils;

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

				return ResponseEntity.ok(resp);
			}
		}
		return null;
	}
}
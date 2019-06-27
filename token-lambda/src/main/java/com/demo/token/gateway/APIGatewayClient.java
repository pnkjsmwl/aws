package com.demo.token.gateway;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.demo.token.dao.Response;

@Component
public class APIGatewayClient {

	@Value("${other.gateway.url}")
	private String other_gateway_url;

	@Value("${token_value}")
	private String tokenValue;

	public Response callAPIGatewayDiffRegion(String jwtToken, String accountId, String action, String redisKey) {

		RestTemplate rt = new RestTemplate();
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
		
		if(jwtToken!=null) requestHeaders.add("Authorization", jwtToken) ;
		if(accountId!=null) requestHeaders.add("AccountId", accountId);
		if(redisKey!=null) requestHeaders.add("Key", redisKey);
		
		requestHeaders.add("Action", action);

		HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

		System.out.println("API Gateway URL : "+other_gateway_url);
		URI uri = UriComponentsBuilder.fromUriString(other_gateway_url)
				.build()
				.toUri();

		ResponseEntity<Response> respEntity = rt.exchange(uri, HttpMethod.GET, requestEntity, Response.class);
		System.out.println("Response status code : "+respEntity.getStatusCode());

		if(respEntity.getStatusCode()==HttpStatus.OK) {
			System.out.println("Response : "+respEntity.getBody());
			Response resp = respEntity.getBody();
			System.out.println("Response Body : "+resp);
			System.out.println("Response Code/Message : "+resp.getCode()+"/"+resp.getMessage());
			return resp;
		}
		return null;
	}
}

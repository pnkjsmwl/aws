package com.bhavani.accounts.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.bhavani.accounts.doc.AccountSummary;

@RestController
public class AccountController {
	@Autowired
	@Qualifier("accountData")
	HashMap<String,AccountSummary> accountData;
	RestTemplate accountCrudRestTemplate = new RestTemplate();

	@RequestMapping(value="/account/summary",method = RequestMethod.GET)
	public AccountSummary getSummary(@RequestHeader("accountNumber") String accountNumber) throws URISyntaxException{
		URI url = new URI("");
		Map<String, String> params = new HashMap<String, String>();
		params.put("accountNumber", accountNumber);
		HttpEntity<?> entity = new HttpEntity<>(params);
		ResponseEntity<AccountSummary> exchange = accountCrudRestTemplate.exchange(url, HttpMethod.GET, entity, AccountSummary.class);
		return exchange.getBody();
	}
}
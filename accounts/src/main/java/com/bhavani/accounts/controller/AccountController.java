package com.bhavani.accounts.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    public AccountSummary getSummary(@RequestHeader("accountNumber") String accountNumber){
        accountCrudRestTemplate.exchange(url, method, requestEntity, responseType);
    }
}
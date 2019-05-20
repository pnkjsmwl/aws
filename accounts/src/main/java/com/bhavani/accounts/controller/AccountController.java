package com.bhavani.accounts.controller;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class AccountsController {
    @Autowired
    @Qualifier("accountData")
    HashMap<String,AccountSummary> accountData;
    RestTemplate accountCrudRestTemplate = new RestTemplate();

    @RequestMapping(value="/account/summary",method = RequestMethod.GET)
    public AccountSummary getSummary(@RequestHeader("accountNumber") String accountNumber){
        accountCrudRestTemplate.exchange(url, method, requestEntity, responseType);
    }
}
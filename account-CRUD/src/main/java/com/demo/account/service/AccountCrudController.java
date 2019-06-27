package com.demo.account.service;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.account.doc.Account;

@RestController
@RequestMapping("/account-crud")
public class AccountCrudController {
	private Log log = LogFactory.getLog(AccountCrudController.class);

	@Autowired
	@Qualifier("accountData")
	private HashMap<String,Account> accountData;

	@GetMapping("/summary")
	public ResponseEntity<?> getAccountSummary(@RequestParam String accountNumber) {
		if(accountNumber!=null) {
			Account account = accountData.get(accountNumber);
			log.info("Response : "+account);
			return ResponseEntity.ok(account);
		}
		return null;
	}
}

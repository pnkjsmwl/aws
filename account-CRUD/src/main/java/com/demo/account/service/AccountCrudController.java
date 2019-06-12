package com.demo.account.service;

import java.util.HashMap;
import java.util.Map;

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

	@Autowired
	@Qualifier("accountData")
	private HashMap<String,Account> accountData;

	@GetMapping("/summary")
	public ResponseEntity<?> getAccountSummary(@RequestParam String accountNumber) {
		if(accountNumber!=null) {
			Account account = accountData.get(accountNumber);
			return ResponseEntity.ok(account);
		}
		return null;
	}

	@GetMapping("/current-balance")
	public ResponseEntity<?> getCurrentBalance(@RequestParam String accountNumber) {
		Map<String,String> map = new HashMap<String,String>();
		if(accountNumber!=null) {
			map.put("current-balance", accountData.get(accountNumber).getCurrentBalance());
			return ResponseEntity.ok(map);
		}
		return null;
	}

	@GetMapping("/due")
	public ResponseEntity<?> getDue(@RequestParam String accountNumber) {
		Map<String,String> map = new HashMap<String,String>();
		if(accountNumber!=null) {
			map.put("due_amount", accountData.get(accountNumber).getDueAmount());
			map.put("due_date", accountData.get(accountNumber).getDueDate());
			return ResponseEntity.ok().body(map);
		}
		return null;
	}
}

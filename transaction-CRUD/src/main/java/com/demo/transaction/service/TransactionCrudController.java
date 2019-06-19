package com.demo.transaction.service;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.transaction.doc.Transaction;

@RestController
@RequestMapping("/transaction-crud")
public class TransactionCrudController {

	
	@Autowired
	@Qualifier("transactionData")
	private Map<String, Deque<Transaction>> transactionData;

	@GetMapping("/summary")
	public ResponseEntity<?> getTrasnactionSummary(@RequestParam String accountNumber) {

		if(accountNumber!=null) {
			Deque<Transaction> value = transactionData.get(accountNumber);
			return ResponseEntity.ok(value);
		}
		return null;
	}

	@GetMapping("/recent")
	public ResponseEntity<?> getRecentNTransactions(@RequestParam String accountNumber, @RequestParam String count) {
		Map<String, List<Transaction>> map = new HashMap<>();
		if(accountNumber!=null) {
			Deque<Transaction> queue = transactionData.get(accountNumber);
			map.put("recent-"+count+"-transactions", queue.stream().limit(Integer.parseInt(count)).collect(Collectors.toList()));
			return ResponseEntity.ok(map);
		}
		return null;
	}

	@GetMapping("/last")
	public ResponseEntity<?> getLastTransaction(@RequestParam String accountNumber) {
		Map<String, Transaction> map = new HashMap<>();
		if(accountNumber!=null) {
			Deque<Transaction> queue = transactionData.get(accountNumber);
			map.put("last-transaction", queue.getFirst());
			return ResponseEntity.ok(map);
		}
		return null;
	}
}

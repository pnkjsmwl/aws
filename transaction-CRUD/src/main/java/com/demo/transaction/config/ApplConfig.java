package com.demo.transaction.config;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.demo.transaction.doc.Transaction;

@Configuration
public class ApplConfig {

	@Bean(name="transactionData")
	public Map<String, Deque<Transaction>> getTransactionData() {
		final Map<String, Deque<Transaction>> transaction = new HashMap<>();
		extracted(new Date(), "1234567812345678", transaction);
		extracted(new Date(), "8765432187654321", transaction);
		extracted(new Date(), "1020304010203040", transaction);

		return transaction;
	}

	private Date date(Date date) {
		LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		localDateTime = localDateTime.plusDays(1);
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	private Map<String, Deque<Transaction>> extracted(Date now, String accountNumber, Map<String, Deque<Transaction>> transaction) {
		Deque<Transaction> deque = new LinkedList<>();

		Date date1 = date(new Date());
		Transaction t1 = new Transaction("asd-a212-dda", accountNumber, date1.toString(), "Samsung S10", "debit", "1099.99", "9128");
		Date date2 = date(date1);
		Transaction t2 = new Transaction("asd-a912-dda", accountNumber, date2.toString(), "iPhone X", "debit", "1199.99", "7261");
		Date date3 = date(date2);
		Transaction t3 = new Transaction("asd-a907-dda", accountNumber, date3.toString(), "Bean bag", "debit", "25", "8261");
		Date date4 = date(date3);
		Transaction t4 = new Transaction("asd-huj7-dda", accountNumber, date4.toString(), "Microsoft Xbox One S", "debit", "206", "6771");
		Date date5 = date(date4);
		Transaction t5 = new Transaction("asd-0mmn-dda", accountNumber, date5.toString(), "LG - 4.5 Cu. Ft. 12-Cycle Front-Loading Smart Wi-Fi Washer with 6Motion Technology", "debit", "799.99", "7262");
		Date date6 = date(date5);
		Transaction t6 = new Transaction("asd-h007-dda", accountNumber, date6.toString(), "Head & Shoulders shampoo for men", "debit", "22", "9191");
		Date date7 = date(date6);
		Transaction t7 = new Transaction("asd-7bd7-dda", accountNumber, date7.toString(), "Amaravati dine", "debit", "65", "987");
		Date date8 = date(date7);
		Transaction t8 = new Transaction("asd-p127-dda", accountNumber, date8.toString(), "Costco gas", "debit", "32", "3452");
		deque.addFirst(t1);deque.addFirst(t2);deque.addFirst(t3);deque.addFirst(t4);deque.addFirst(t5);deque.addFirst(t6);deque.addFirst(t7);deque.addFirst(t8);

		transaction.put(accountNumber, deque);

		return transaction;
	}
}

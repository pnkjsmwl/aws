package com.demo.account.config;

import java.util.HashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.demo.account.doc.Account;

@Configuration
public class ApplConfig {

	@Bean(name="accountData")
	public HashMap<String,Account> getAccountData() {
		HashMap<String,Account> data = new HashMap<>();
		Account Pankaj = new Account();
		Pankaj.setId("1");
		Pankaj.setAccountNumber("1234567812345678");
		Pankaj.setCurrentBalance("1212");
		Pankaj.setDueAmount("300");
		Pankaj.setDueDate("06-20-2019");

		Account Ganesh = new Account();
		Ganesh.setId("2");
		Ganesh.setAccountNumber("8765432187654321");
		Ganesh.setCurrentBalance("4553");
		Ganesh.setDueAmount("300");
		Ganesh.setDueDate("06-21-2019");

		Account Bhavani = new Account();
		Bhavani.setId("3");
		Bhavani.setAccountNumber("1020304010203040");
		Bhavani.setCurrentBalance("5364");
		Bhavani.setDueAmount("300");
		Bhavani.setDueDate("06-22-2019");

		data.put(Pankaj.getAccountNumber(), Pankaj);
		data.put(Ganesh.getAccountNumber(), Ganesh);
		data.put(Bhavani.getAccountNumber(), Bhavani);

		return data;
	}

}

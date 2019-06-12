package com.demo.transaction.doc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

	private String id;
	private String accountNumber;
	private String date;
	private String description;
	private String type;
	private String amount;
	private String balance;
}

package com.bhavani.accounts.doc;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class AccountSummary {
	
	@Id
	private String id;
	private String accountNumber;
	private String currentBalance;
	private String dueAmount;
	@Indexed
	private String dueDate;

}

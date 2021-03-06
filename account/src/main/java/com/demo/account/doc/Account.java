package com.demo.account.doc;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account implements  Serializable{
	private static final long serialVersionUID = 1L;

	private String id;
	private String accountNumber;
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(as = String.class)
	private String currentBalance;
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(as = String.class)
	private String dueAmount;
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(as = String.class)
	private String dueDate;
}

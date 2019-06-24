package com.demo.transaction.doc;

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
public class Transaction implements  Serializable{
	private static final long serialVersionUID = 1L;

	private String id;
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(as = String.class)
	private String accountNumber;
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(as = String.class)
	private String date;
	private String description;
	private String type;
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(as = String.class)
	private String amount;
	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(as = String.class)
	private String balance;
}

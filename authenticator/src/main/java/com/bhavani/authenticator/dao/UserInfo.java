package com.bhavani.authenticator.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document
public class UserInfo{
	@Id
	private String id;
	@Indexed
	private String userName;
	private String password;
	@Indexed
	private String accountNumber;
	@Indexed
	private String role;

}
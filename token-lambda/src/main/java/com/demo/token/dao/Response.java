package com.demo.token.dao;

import java.util.List;
import java.util.Map;

public class Response {

	private String message;
	private List<Map<String, String>> account;
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public List<Map<String, String>> getAccount() {
		return account;
	}
	public void setAccount(List<Map<String, String>> account) {
		this.account = account;
	}
	
	
}

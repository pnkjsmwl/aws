package com.demo.token.dao;

import java.util.List;
import java.util.Map;

public class Response {

	private String message;
	private String status;
	private String error;
	private String code;
	private Map<String,String> map;
	private List<Map<String, String>> account;

	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public Map<String, String> getMap() {
		return map;
	}
	public void setMap(Map<String, String> map) {
		this.map = map;
	}
	public List<Map<String, String>> getAccount() {
		return account;
	}
	public void setAccount(List<Map<String, String>> account) {
		this.account = account;
	}

}

package com.demo.token.dao;

import java.util.Map;

public class TokenAuthRequest {

	String type;
	String authorizationToken;
	String methodArn;
	Map<String,String> headers;

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAuthorizationToken() {
		return authorizationToken;
	}
	public void setAuthorizationToken(String authorizationToken) {
		this.authorizationToken = authorizationToken;
	}
	public String getMethodArn() {
		return methodArn;
	}
	public void setMethodArn(String methodArn) {
		this.methodArn = methodArn;
	}
	public Map<String, String> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}


}

package com.demo.token.dao;

public class TokenAuthRequest {

	String type;
    String authorizationToken;
    String methodArn;
	
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
    
    
	
	
}

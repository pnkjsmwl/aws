package com.demo.token.dao;

public class Caller{
/*    private String name;
    private String resource;
    private String httpVerb;
    private String authorizationToken;
    private Map<String,String> headers = new HashMap<>();*/
    
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
package com.demo.token.dao;

import java.util.HashMap;
import java.util.Map;

public class Caller{
    private String name;
    private String resource;
    private String httpVerb;
    private Map<String,String> headers = new HashMap<>();
    
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getResource() {
		return resource;
	}
	public void setResource(String resource) {
		this.resource = resource;
	}
	public String getHttpVerb() {
		return httpVerb;
	}
	public void setHttpVerb(String httpVerb) {
		this.httpVerb = httpVerb;
	}
	public Map<String, String> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
    
    
}
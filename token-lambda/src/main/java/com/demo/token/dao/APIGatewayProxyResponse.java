package com.demo.token.dao;

import java.util.Map;

public class APIGatewayProxyResponse {

	private int statusCode;
	private Map<String, String> headers;
	private String body;
	private boolean isBase64Encoded;

	public APIGatewayProxyResponse() {
	}

	public int getStatusCode() {
		return statusCode;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public String getBody() {
		return body;
	}

	public boolean getIsBase64Encoded() {
		return isBase64Encoded;
	}

	public APIGatewayProxyResponse(int statusCode, Map<String, String> headers, String body, boolean isBase64Encoded) {
		this.statusCode = statusCode;
		this.headers = headers;
		this.body = body;
		this.isBase64Encoded = isBase64Encoded;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void setBase64Encoded(boolean isBase64Encoded) {
		this.isBase64Encoded = isBase64Encoded;
	}



}


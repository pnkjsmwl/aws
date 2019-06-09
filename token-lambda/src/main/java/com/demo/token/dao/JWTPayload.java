package com.demo.token.dao;

import java.util.Date;
import java.util.List;

public class JWTPayload{
    private String issuer;
    private String subject;
    private List<String> audience;
    private Date expirationTime;
    private Date issueTime;
    private String accountNumber;
    private String jwtID;
    private String role;
    private String userName;
	public String getIssuer() {
		return issuer;
	}
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public List<String> getAudience() {
		return audience;
	}
	public void setAudience(List<String> audience) {
		this.audience = audience;
	}
	public Date getExpirationTime() {
		return expirationTime;
	}
	public void setExpirationTime(Date expirationTime) {
		this.expirationTime = expirationTime;
	}
	public Date getIssueTime() {
		return issueTime;
	}
	public void setIssueTime(Date issueTime) {
		this.issueTime = issueTime;
	}
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public String getJwtID() {
		return jwtID;
	}
	public void setJwtID(String jwtID) {
		this.jwtID = jwtID;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
    
    
    
}
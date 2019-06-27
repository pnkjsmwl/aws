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
	private boolean valid;
	private String redisKey;
	private String regionCreated;
	private String regionLatest;
	private boolean foundInDiffRegion;
	private String multiRegion;

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
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	public String getRedisKey() {
		return redisKey;
	}
	public void setRedisKey(String redisKey) {
		this.redisKey = redisKey;
	}
	public String getRegionCreated() {
		return regionCreated;
	}
	public void setRegionCreated(String regionCreated) {
		this.regionCreated = regionCreated;
	}
	public String getRegionLatest() {
		return regionLatest;
	}
	public void setRegionLatest(String regionLatest) {
		this.regionLatest = regionLatest;
	}
	public boolean isFoundInDiffRegion() {
		return foundInDiffRegion;
	}
	public void setFoundInDiffRegion(boolean foundInDiffRegion) {
		this.foundInDiffRegion = foundInDiffRegion;
	}
	public String getMultiRegion() {
		return multiRegion;
	}
	public void setMultiRegion(String multiRegion) {
		this.multiRegion = multiRegion;
	}
}
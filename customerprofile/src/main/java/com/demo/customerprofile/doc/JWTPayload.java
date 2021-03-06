package com.demo.customerprofile.doc;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class JWTPayload{
	private String issuer;
	private String subject;
	private List<String> audience;
	private Date expirationTime;
	private Date issueTime;
	private String accountNumber;
	private String jwtID;
	private String userName;
	private String role;
}
package com.demo.token.authenticator.exception;

public class InvalidTokenException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidTokenException(String mesg, Throwable t) {
		super(mesg, t);
	}
	
	public InvalidTokenException(String mesg) {
		super(mesg);
	}

	public InvalidTokenException() {
		super();
	}
}

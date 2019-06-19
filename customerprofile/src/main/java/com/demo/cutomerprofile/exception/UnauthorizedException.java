package com.demo.cutomerprofile.exception;

public class UnauthorizedException extends Exception {

	private static final long serialVersionUID = 1L;

	public UnauthorizedException(String mesg, Throwable t) {
		super(mesg, t);
	}

	public UnauthorizedException() {
		super();
	}

}

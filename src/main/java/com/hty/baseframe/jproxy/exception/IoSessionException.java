package com.hty.baseframe.jproxy.exception;


public class IoSessionException extends RuntimeException {
	
	private static final long serialVersionUID = 2277795376483084907L;

	public IoSessionException() {
		super();
	}
	
	public IoSessionException(Exception e) {
		super(e);
	}
	
	public IoSessionException(String message) {
		super(message);
	}
}

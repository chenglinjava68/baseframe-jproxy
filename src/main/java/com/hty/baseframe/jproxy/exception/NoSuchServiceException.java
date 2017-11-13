package com.hty.baseframe.jproxy.exception;

/**
 * 找不到服务异常
 * @author Tisnyi
 */
public class NoSuchServiceException extends IllegalStateException {

	private static final long serialVersionUID = -6251985546315313325L;
	
	public NoSuchServiceException() {
	}
	public NoSuchServiceException(Exception e) {
		super(e);
	}
	public NoSuchServiceException(String messgae) {
		super(messgae);
	}
}
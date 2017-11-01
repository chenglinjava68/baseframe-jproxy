package com.hty.baseframe.jproxy.exception;

/**
 * 服务调用异常
 * @author Tisnyi
 */
public class ServiceInvokationException extends IllegalStateException {
	private static final long serialVersionUID = -6251985546315313325L;
	
	public ServiceInvokationException() {
	}
	public ServiceInvokationException(Exception e) {
		super(e);
	}
	public ServiceInvokationException(String messgae) {
		super(messgae);
	}
}
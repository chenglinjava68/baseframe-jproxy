package com.hty.baseframe.jproxy.exception;
/**
 * 配置错误异常
 * @author Tisnyi
 */
public class IllegalConfigurationException extends IllegalStateException {
	private static final long serialVersionUID = -6251985546315313325L;
	
	public IllegalConfigurationException() {
	}
	public IllegalConfigurationException(Exception e) {
		super(e);
	}
	public IllegalConfigurationException(String messgae) {
		super(messgae);
	}
}

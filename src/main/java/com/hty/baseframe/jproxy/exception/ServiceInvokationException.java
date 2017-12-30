package com.hty.baseframe.jproxy.exception;

/**
 * 服务调用异常
 *
 * @author Hetianyi 2017/12/30
 * @version 1.0
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
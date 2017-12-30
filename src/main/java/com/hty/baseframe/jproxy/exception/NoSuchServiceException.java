package com.hty.baseframe.jproxy.exception;

/**
 * 找不到服务异常
 *
 * @author Hetianyi 2017/12/30
 * @version 1.0
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
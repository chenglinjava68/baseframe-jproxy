package com.hty.baseframe.jproxy.common;

/**
 * 系统常量
 *
 * @author Hetianyi 2017/12/30
 * @version 1.0
 */
public class Const {
    /**
     * 请求头大小, 最大传输字节数为99999999/1024/1024 = 95.367MB
     */
    public static final int REQUEST_HEAD_SIZE = 8;
    /**
     * 序列化对象标识：请求标识
     */
    public static final byte REQUEST_MARK = 1;
    /**
     * 序列化对象标识：响应标识
     */
    public static final byte RESPONSE_MARK = 2;
}

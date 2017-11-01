package com.hty.baseframe.jproxy.bean;

import java.io.Serializable;
/**
 * 请求响应
 * @author Tisnyi
 */
public class ServiceResponse implements Serializable {

	private static final long serialVersionUID = 5829269909837481630L;
	
	public static final int SUCCESS = 1;//成功
	public static final int ERROR_REQUEST_HEAD = 10;//请求头不正确
	public static final int ERROR_REQUEST_BODY = 11;//请求体不正确
	public static final int ERROR_RESPONSE_HEAD = 12;//请求头不正确
	public static final int ERROR_RESPONSE_BODY = 13;//请求体不正确
	public static final int PERMISSION_DENIE_IP = 20;//请求不允许:IP地址不允许调用
	public static final int PERMISSION_DENIE_TOKEN = 21;//请求不允许：token验证失败 
	public static final int INNER_ERROR = 30;//内部错误（业务逻辑错误）
	
	
	/** 处理结果信号代码 */
	private int code;
	/** 处理结果 */
	private Object result;
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
}

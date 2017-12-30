package com.hty.baseframe.jproxy.bean;

import java.io.Serializable;
/**
 * 调用服务方法的载体
 * @author Hetianyi 2017/12/30
 * @version 1.0
 */
public class MethodEntity implements Serializable {

	private static final long serialVersionUID = 6071213128946294094L;
	/**
	 * 调用方法所在类的名称:class.getName() 
	 */
	private Class<?> objectClass;
	/**
	 * 调用的方法名
	 */
	private String methodName;
	/**
	 * 调用的方法参数
	 */
	private Object[] args;
	/**
	 * 参数class类型
	 */
	private Class<?>[] argsTypes;
	
	
	public Class<?> getObjectClass() {
		return objectClass;
	}
	public void setObjectClass(Class<?> objectClass) {
		this.objectClass = objectClass;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public Object[] getArgs() {
		return args;
	}
	public void setArgs(Object[] args) {
		this.args = args;
	}
	public Class<?>[] getArgsTypes() {
		return argsTypes;
	}
	public void setArgsTypes(Class<?>[] argsTypes) {
		this.argsTypes = argsTypes;
	}
}

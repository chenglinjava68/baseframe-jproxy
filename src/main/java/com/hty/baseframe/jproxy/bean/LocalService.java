package com.hty.baseframe.jproxy.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
/**
 * 本地服务（本地对外提供的服务）。
 * @author Hetianyi 2017/12/30
 * @version 1.0
 */
public class LocalService implements Serializable {

	private static final long serialVersionUID = -5055420941173439886L;
	/** 本地接口类 */
	private final Class<?> clazz;
	/**
	 * 注册中心ID，如果此项不为空，则从注册中心获取服务地址，
	 * 否则直接使用RemoteService的host:port直连 
	 * */
	private final String centerId;
	/**
	 * 匹配条件
	 */
	private final Map<String, String> conditions = new HashMap<String, String>();
	/**
	 * 默认构造方法
	 * @param clazz 服务接口类
	 * @param centerId 注册中心
	 */
	public LocalService(Class<?> clazz, String centerId) {
		this.clazz = clazz;
		this.centerId = centerId;
	}

	public String addCondition(String name, String value) {
		return conditions.put(name, value);
	}
	
	public Map<String, String> getConditions() {
		return conditions;
	}

	public Class<?> getClazz() {
		return clazz;
	}
	@Override
	public String toString() {
		return "{class:'"+ clazz.getName() +"', centerId:'"+ centerId +"'}";
	}

	public String getCenterId() {
		return centerId;
	}
}

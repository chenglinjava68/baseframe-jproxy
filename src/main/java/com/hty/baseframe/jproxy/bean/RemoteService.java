package com.hty.baseframe.jproxy.bean;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 远程服务（本地调用远程主机提供的服务）。
 * @author Hetianyi 2017/12/30
 * @version 1.0
 */
public class RemoteService {
	/** 服务接口类 */
	private final Class<?> clazz;
	/** 远程服务地址 */
	private final String host;
	/** 远程服务端口*/
	private int port;
	/**
	 * 注册中心ID，如果此项不为空，则从注册中心获取服务地址，
	 * 否则直接使用RemoteService的host:port直连 
	 * */
	private final String centerId;
	/**
	 * 匹配条件，如果获取代理类时没有条件，使用配置文件中的匹配条件，
	 * 如果获取代理类时存在条件，则使用给定的条件。
	 */
	private final Map<String, String> conditions = new HashMap<String, String>();
	
	/**
	 * 默认构造方法，如果给出centerId，则host和port参数可为空，否则需要给出host和port参数
	 * @param clazz 服务接口类
	 * @param host 远程服务主机地址
	 * @param _port 远程服务主机端口（字符串）
	 * @param centerId 注册中心
	 */
	public RemoteService(Class<?> clazz, String host, String _port, String centerId) {
		this.clazz = clazz;
		this.host = host;
		try {
			this.port = Integer.valueOf(_port);
		} catch (NumberFormatException e) {
			this.port = 0;
		}
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
	public String getHost() {
		return host;
	}
	public int getPort() {
		return port;
	}
	
	@Override
	public String toString() {
		return "{clazz:'"+ clazz.getName() 
				+"', host:'"+ host +"', port:'"+ port +"', centerId:'"+ centerId +"'}";
	}

	public String getCenterId() {
		return centerId;
	}

	/**
	 * 克隆此RemoteService的一个副本用于动态获取代理服务
	 * @param conditions 条件
	 * @return
	 */
	public RemoteService clone(Map<String, String> conditions) {
		RemoteService rs = new RemoteService(this.clazz, this.host, String.valueOf(this.port), this.centerId);
		if(null != conditions && !conditions.isEmpty()) {
			for(Iterator<String> it = conditions.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				rs.addCondition(key, conditions.get(key));
			}
		}
		return rs;
	}
} 

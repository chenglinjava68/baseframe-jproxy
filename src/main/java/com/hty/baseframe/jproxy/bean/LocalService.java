package com.hty.baseframe.jproxy.bean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hty.baseframe.common.util.StringUtil;
/**
 * 本地服务（本地对外提供的服务）。<br>
 * @author Tisnyi
 * @version 1.0
 */
public class LocalService {
	/** 接口调用token */
	private final String token;
	/** 本地接口类 */
	private final Class<?> clazz;
	/** 接口版本 */
	private final String version;
	/** 
	 * 注册中心ID，如果此项不为空，则从注册中心获取服务地址，
	 * 否则直接使用RemoteService的host:port直连 
	 * */
	private final String centerId;
	
	/** 服务白名单 */
	private final Set<String> clients = new HashSet<String>();
	
	private final Map<String, String> conditions = new HashMap<String, String>();
	/**
	 * 默认构造方法
	 * @param id 服务ID，用于本地和远程标识
	 * @param token 服务token，调用服务需要校验此参数
	 * @param clazz 服务接口类
	 */
	public LocalService(Class<?> clazz, String version, String token, String centerId) {
		this.clazz = clazz;
		this.token = StringUtil.trim(token);
		this.version = version;
		this.centerId = centerId;
	}
	
	public String getToken() {
		return token;
	}
	public Set<String> getClients() {
		return clients;
	}
	/**
	 * 添加一个白名单
	 * @param client
	 */
	public void addClient(String client) {
		if(!StringUtil.isEmpty(client)) {
			this.clients.add(client);
		}
	}
	
	public String addCondition(String name, String value) {
		return conditions.put(name, value);
	}
	
	public Map<String, String> getConditions() {
		return conditions;
	}
	public String getCondition(String name) {
		return conditions.get(name);
	}
	
	
	public Class<?> getClazz() {
		return clazz;
	}
	@Override
	public String toString() {
		return "{class:'"+ clazz.getName() +"', token:'"+ token +"', version:'"+ version +"'}";
	}

	public String getVersion() {
		return version;
	}

	public String getCenterId() {
		return centerId;
	}
}

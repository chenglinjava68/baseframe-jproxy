package com.hty.baseframe.jproxy.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * 远程服务（本地调用远程主机提供的服务）。<br>
 * @author Tisnyi
 * @version 1.0
 */
public class RemoteService {
	/** 服务接口类 */
	private final Class<?> clazz;
	/** 接口版本 */
	private final String version;
	/** 远程服务地址 */
	private final String host;
	/** 远程服务端口*/
	private int port;
	/** 接口调用token */
	private final String token;
	/** 为此service保持的最大socket线程池大小 */
	private Integer poolsize;
	/** 
	 * 注册中心ID，如果此项不为空，则从注册中心获取服务地址，
	 * 否则直接使用RemoteService的host:port直连 
	 * */
	private final String centerId;

	private final Map<String, String> conditions = new HashMap<String, String>();
	
	/**
	 * 默认构造方法
	 * @param id 远程服务的本地标识ID，用于标识远程服务
	 * @param serviceId 远程服务ID，用于标识对应的远程服务
	 * @param clazz 服务接口类
	 * @param host 远程服务主机地址
	 * @param port 远程服务主机端口
	 * @param token 服务token
	 * @param poolsize 服务socket池大小
	 */
	public RemoteService(Class<?> clazz, String host, String _port, String token, 
			int poolsize, String version, String centerId) {
		this.clazz = clazz;
		this.host = host;
		try {
			this.port = Integer.valueOf(_port);
		} catch (NumberFormatException e) {
			this.port = 0;
		}
		this.token = token;
		this.poolsize = poolsize;
		this.version = version;
		this.centerId = centerId;
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
	public String getToken() {
		return token;
	}
	public Integer getPoolsize() {
		return poolsize;
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
				+"', host:'"+ host +"', port:'"+ port +"', token:'"+ token 
				+"', poolsize:'"+ poolsize +"', centerId:'"+ centerId +"'}";
	}

	public String getVersion() {
		return version;
	}

	public String getCenterId() {
		return centerId;
	}
} 

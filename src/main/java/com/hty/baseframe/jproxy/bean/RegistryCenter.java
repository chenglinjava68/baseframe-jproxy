package com.hty.baseframe.jproxy.bean;

import java.io.Serializable;

/**
 * 注册中心Bean
 * @author Hetianyi 2017/12/30
 * @version 1.0
 */
public class RegistryCenter implements Serializable {

	private static final long serialVersionUID = -4304059914999554370L;
	/** 注册中心ID */
	private final String id;
	/** 注册中心地址 */
	private final String host;
	/** 注册中心端口 */
	private final int port;
	
	public RegistryCenter(String id, String host, int port) {
		this.id = id;
		this.host = host;
		this.port = port;
	}

	public String getId() {
		return id;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
	@Override
	public String toString() {
		return "{id:'"+ id +"', host:'"+ host +"', port:'"+ port +"'}";
	}
	
}

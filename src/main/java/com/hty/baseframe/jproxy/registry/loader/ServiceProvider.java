package com.hty.baseframe.jproxy.registry.loader;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.hty.baseframe.common.util.StringUtil;
import com.hty.baseframe.jproxy.bean.LocalService;
import com.hty.baseframe.jproxy.common.SysProprties;
import com.hty.baseframe.jproxy.server.ServerSocketManager;
import com.hty.baseframe.jproxy.util.NetWorkInterfaceUtil;
/**
 * 服务提供者Bean，在提供者启动时向注册中心定时注册，暴露服务的载体
 * @author Tisnyi
 */
public class ServiceProvider implements Serializable {
	private static final long serialVersionUID = 2305179703813320720L;
	/** 服务接口类 */
	private String clazz;
	/** 服务提供者网络IP地址列表，可能存在多个地址 */
	private Set<String> addresses;
	/** 服务提供者的白名单 */
	private Set<String> whitelist;
	/** token */
	private String token;
	/** 服务提供版本 */
	private String version;
	/** 服务端口 */
	private int port;
	/** 此参数由注册中心设置 */
	private String providerLookBackAddress;
	/** 此参数由注册中心设置, 命中次数 */
	private int hitcount = 0;

	private Map<String, String> conditions = new HashMap<String, String>();
	
	public ServiceProvider() {
	}
	
	public ServiceProvider(LocalService ls) {
		this.clazz = ls.getClazz().getName();
		this.port = Integer.valueOf(SysProprties.getProperty("local_service_port"));
		this.whitelist = ls.getClients();
		this.token = null == ls.getToken() ? null : StringUtil.trim(ls.getToken());
		this.version = null == ls.getVersion() ? null : StringUtil.trim(ls.getVersion());
		this.conditions = ls.getConditions();
		this.addresses = NetWorkInterfaceUtil.getLocalHostAddress();
	}
	
	/**
	 * 将该对象克隆为CandidateProvider返回给消费者
	 */
	public CandidateProvider clone() {
		CandidateProvider provider = new CandidateProvider();
		provider.setAddresses(this.addresses);;
		provider.setClazz(this.clazz);
		provider.setProviderLookBackAddress(this.providerLookBackAddress);
		provider.setVersion(this.version);
		provider.setPort(this.port);
		provider.setConsumerLookBackAddress(((InetSocketAddress) ServerSocketManager.getCurrentSocket().getRemoteSocketAddress()).getAddress().getHostAddress());
		return provider;
	}

	public Map<String, String> getConditions() {
		return conditions;
	}
	
	@Override
	public String toString() {
		return "{class:'"+ clazz +"', version:'"+ version +"', port:'"+ port +"', addresses: '"+ addresses +"', providerLookBackAddress: '"+ providerLookBackAddress +"'}";
	}
	/**
	 * 获取该提供者的唯一标识
	 * @return
	 */
	public String getUUID() {
		return this.providerLookBackAddress + ":" + this.port + ":" + this.clazz + ":" + this.version;
	}


	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public Set<String> getAddresses() {
		return addresses;
	}

	public void setAddresses(Set<String> addresses) {
		this.addresses = addresses;
	}

	public Set<String> getWhitelist() {
		return whitelist;
	}

	public void setWhitelist(Set<String> whitelist) {
		this.whitelist = whitelist;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getProviderLookBackAddress() {
		return providerLookBackAddress;
	}

	public void setProviderLookBackAddress(String providerLookBackAddress) {
		this.providerLookBackAddress = providerLookBackAddress;
	}

	public int getHitcount() {
		return hitcount;
	}

	public void setHitcount(int hitcount) {
		this.hitcount = hitcount;
	}
}
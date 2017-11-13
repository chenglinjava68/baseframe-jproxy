package com.hty.baseframe.jproxy.registry.loader;

import com.hty.baseframe.common.util.StringUtil;
import com.hty.baseframe.jproxy.bean.RemoteService;
import com.hty.baseframe.jproxy.util.NetWorkInterfaceUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
/**
 * 服务消费者Bean，在消费者需要获取服务时向注册中心发送请求的载体。
 * @author Tisnyi
 */
public final class ServiceConsumer implements Serializable {

	private static final long serialVersionUID = -6505618265286596969L;
	/** 服务接口类 */
	private String clazz;
	/** 服务消费者网络IP地址列表，可能存在多个地址 */
	private Set<String> addresses;
	/** token */
	private String token;
	/**
	 * 此参数由注册中心设置 ,当提供者和消费者的注册中心检测IP相同时可以匹配彼此
	 **/
	private String consumerAddress;
	
	private Map<String, String> conditions = new HashMap<String, String>();
	
	public ServiceConsumer() {
	}
	
	public ServiceConsumer(RemoteService service) {
		this.clazz = service.getClazz().getName();
		this.token = null == service.getToken() ? null : StringUtil.trim(service.getToken());
		this.conditions = service.getConditions();
		this.addresses = NetWorkInterfaceUtil.getLocalHostAddress();
	}
	
	public Map<String, String> getConditions() {
		return conditions;
	}
	
	public String getClazz() {
		return clazz;
	}
	public Set<String> getAddresses() {
		return addresses;
	}
	public String getToken() {
		return token;
	}

	public String getConsumerAddress() {
		return consumerAddress;
	}

	public void setConsumerAddress(String consumerAddress) {
		this.consumerAddress = consumerAddress;
	}
	
	@Override
	public String toString() {
		return "{clazz: '"+ clazz +"', token: '"+ token
				+"', addresses: '"+ addresses +"', consumerAddress: '"+ consumerAddress +"'}";
	}
}

package com.hty.baseframe.jproxy.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hty.baseframe.jproxy.bean.RegistryCenter;
import com.hty.baseframe.jproxy.bean.RemoteService;
import com.hty.baseframe.jproxy.exception.IllegalConfigurationException;
import com.hty.baseframe.jproxy.registry.ServiceRegistryServer;

/**
 * 注册中心工厂类
 * @author Tisnyi
 */
public class RegistryFactory {
	
	private Log logger = LogFactory.getLog(RegistryFactory.class);
	/** 单实例 */
	private static RegistryFactory registryFactory;
	
	private static Map<String, RegistryCenter> centers = new HashMap<String, RegistryCenter>();
	/** 构造方法私有化 */
	private RegistryFactory() {
	}
	/**
	 * 获取单实例的RegistryFactory
	 * @return RegistryFactory
	 */
	public static synchronized RegistryFactory getInstance() {
		if(null == registryFactory) {
			registryFactory = new RegistryFactory();
		}
		return registryFactory;
	}
	
	/**
	 * 添加一个注册中心
	 * @param center 注册中心Bean
	 */
	public synchronized void addRegistryCenter(RegistryCenter center) {
		if(null == center) {
			throw new IllegalConfigurationException("Can not add a null RegistryCenter.");
		}
		if(!centers.containsKey(center.getId())) {
			logger.info("Adding RegistryCenter: " + center);
			//没添加一个注册中心，自动添加一个特殊的RemoteService（注册服务）
			//注册服务最多只给一个socket连接，且token，version，centerId均为空
			RemoteService rs = new RemoteService(ServiceRegistryServer.class,
					center.getHost(), String.valueOf(center.getPort()), null, 1, center.getId(), null);
			ServiceFactory.addRemoteService(rs);
			centers.put(center.getId(), center);
		} else {
			logger.error("RegistryCenter with id : " + center.getId() + " already exists, will not add.");
		}
	}
	/**
	 * 获取注册中心
	 * @param id 注册中心ID
	 * @return
	 */
	public RegistryCenter getRegistryCenter(String id) {
		return centers.get(id);
	}
	
}

package com.hty.baseframe.jproxy.registry.impl;

import com.hty.baseframe.jproxy.bean.LocalService;
import com.hty.baseframe.jproxy.bean.RemoteService;
import com.hty.baseframe.jproxy.common.ServiceFactory;
import com.hty.baseframe.jproxy.registry.ServiceRegistryClient;
import com.hty.baseframe.jproxy.registry.ServiceRegistryServer;
import com.hty.baseframe.jproxy.registry.loader.CandidateProvider;
import com.hty.baseframe.jproxy.registry.loader.ServiceConsumer;
import com.hty.baseframe.jproxy.registry.loader.ServiceProvider;

public class JProxyServiceRegistry implements ServiceRegistryClient {
	
	//private final Log logger = LogFactory.getLog(JProxyServiceRegistry.class);
	
	private static JProxyServiceRegistry registryClient;
	
	/**
	 * 构造私有化
	 */
	private JProxyServiceRegistry() {
	}
	
	public static synchronized JProxyServiceRegistry getInstance() {
		if(null == registryClient) {
			registryClient = new JProxyServiceRegistry();
		}
		return registryClient;
	}
	
	
	@Override
	public boolean register(LocalService ls, ServiceProvider provider) {
		ServiceRegistryServer regServer = ServiceFactory.getProxyInstance(ServiceRegistryServer.class, ls.getCenterId());
		return regServer.expose(provider);
	}

	@Override
	public CandidateProvider getAvailableService(RemoteService rs, ServiceConsumer consumer) {
		ServiceRegistryServer regServer = ServiceFactory.getProxyInstance(ServiceRegistryServer.class, rs.getCenterId());
		CandidateProvider provider = regServer.find(consumer);
		return provider;
	}

	
}

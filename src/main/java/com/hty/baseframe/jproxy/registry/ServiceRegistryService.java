package com.hty.baseframe.jproxy.registry;

import com.hty.baseframe.jproxy.registry.loader.CandidateProvider;
import com.hty.baseframe.jproxy.registry.loader.ServiceConsumer;
import com.hty.baseframe.jproxy.registry.loader.ServiceProvider;

/**
 * 该接口在注册中心实现，是一种特殊的RemoteService。
 * 该服务的只能同时连接一个注册中心
 * @author Tisnyi
 */
public interface ServiceRegistryService {
	/**
	 * 服务提供者暴露服务
	 * @param provider
	 */
	public boolean expose(ServiceProvider provider);
	/**
	 * 消费者获取服务
	 * @param consumer
	 */
	public CandidateProvider find(ServiceConsumer consumer);
}

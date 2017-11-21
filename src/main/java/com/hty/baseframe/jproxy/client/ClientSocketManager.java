package com.hty.baseframe.jproxy.client;

import com.hty.baseframe.jproxy.bean.RegistryCenter;
import com.hty.baseframe.jproxy.bean.RemoteService;
import com.hty.baseframe.jproxy.common.RegistryFactory;
import com.hty.baseframe.jproxy.exception.IoSessionException;
import com.hty.baseframe.jproxy.exception.ServiceInvokationException;
import com.hty.baseframe.jproxy.registry.ServiceRegistryService;
import com.hty.baseframe.jproxy.registry.impl.JProxyServiceRegistry;
import com.hty.baseframe.jproxy.registry.loader.CandidateProvider;
import com.hty.baseframe.jproxy.registry.loader.ServiceConsumer;
import com.hty.baseframe.jproxy.tunel.client.ClientTunnel;
import com.hty.baseframe.jproxy.util.NetWorkInterfaceUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 客户端Socket管理类，控制每个远程服务的Socket连接状态，连接数，创建新的连接等。
 * 解决了多线程并发问题，保证在高密度请求下Socket连接不会泄漏。
 * @author Tisnyi
 */
public class ClientSocketManager {
	
	private final static Log logger = LogFactory.getLog(ClientSocketManager.class);
	/** 单实例 */
	private static ClientSocketManager clientSocketManager;

	private Map<RemoteService, ClientTunnel> tunnelMap = new HashMap<RemoteService, ClientTunnel>(5);

	public static synchronized ClientSocketManager getInstance() {
		if(null == clientSocketManager) {
			clientSocketManager = new ClientSocketManager();
		}
		return clientSocketManager;
	}

	public ClientTunnel getTunnel(RemoteService rs) {
		ClientTunnel tunnel = tunnelMap.get(rs);
		if(null == tunnel) {
			synchronized (rs) {
				tunnel = tunnelMap.get(rs);
				if(null == tunnel) {
					tunnel = createTunnel(rs);
					tunnelMap.put(rs, tunnel);
				}
			}
		}
		return tunnel;
	}

	private ClientTunnel createTunnel(RemoteService service) {
		//没有配置注册中心的远程服务，直连服务提供者主机
		ClientTunnel tunnel;
		if(null == service.getCenterId() || service.getClazz() == ServiceRegistryService.class) {
			logger.debug("trying to connect directly to remote host.");
			try {
				InetSocketAddress address;
				if(service.getClazz() == ServiceRegistryService.class) {
					RegistryCenter center = RegistryFactory.getInstance().getRegistryCenter(service.getCenterId());
					address = new InetSocketAddress(center.getHost(), center.getPort());
				} else {
					address = new InetSocketAddress(service.getHost(), service.getPort());
				}
				tunnel = new ClientTunnel(address);
				return tunnel;
			} catch (IoSessionException e) {
				throw e;
			}
		} else {
			logger.debug("trying to get provider from registry center.");
			ServiceConsumer consumer;
			CandidateProvider provider;
			Map<String, String> conditions = service.getConditions();
			if(null == conditions || conditions.isEmpty()) {
				consumer = new ServiceConsumer(service);
				provider = JProxyServiceRegistry.getInstance().getAvailableService(service, consumer);
			} else {
				RemoteService rs = service.clone(conditions);
				consumer = new ServiceConsumer(rs);
				provider = JProxyServiceRegistry.getInstance().getAvailableService(rs, consumer);
			}
			logger.info("provider was successfully obtained: " + provider);
			if(null != provider) {
				Set<String> excludes = new HashSet<String>();
				while(true) {
					String candidate = NetWorkInterfaceUtil.pickHostCandidate(consumer, provider, excludes);
					logger.info("select candidate ip: " + candidate);
					if(null == candidate) {
						throw new ServiceInvokationException("No provider available for service: " + service.getClazz().getName());
					}
					logger.info("trying to connect to candidate host: " + candidate + ":" + provider.getPort());
					try {
						InetSocketAddress address = new InetSocketAddress(candidate, provider.getPort());
						tunnel = new ClientTunnel(address);
						logger.info("successfully connected to remote host: " + candidate);
						return tunnel;
					} catch (Exception e) {
						e.printStackTrace();
						excludes.add(candidate);
						logger.info("candidate ip is not available: " + candidate);
						continue;
					}
				}
			}
			throw new ServiceInvokationException("No provider for service: " + service.getClazz().getName());
		}
	}
}

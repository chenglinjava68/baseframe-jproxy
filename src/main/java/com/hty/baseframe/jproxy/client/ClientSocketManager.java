package com.hty.baseframe.jproxy.client;

import com.hty.baseframe.common.util.StringUtil;
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
import java.util.*;

/**
 * 客户端连接管理类，控制每个RemoteService的连接key信息的注册和获取，提供了统一的连接管理。
 * @author Tisnyi
 */
public class ClientSocketManager {
	
	private final static Log logger = LogFactory.getLog(ClientSocketManager.class);
	/** 单实例 */
	private static ClientSocketManager clientSocketManager;

	private static final Map<RemoteService, String> tunnelMap =
            Collections.synchronizedMap(new HashMap<RemoteService, String>(5));

	static synchronized ClientSocketManager getInstance() {
		if(null == clientSocketManager) {
            clientSocketManager = new ClientSocketManager();
		}
		return clientSocketManager;
	}

	String getTunnelKey(RemoteService rs) throws Exception {
        String tunnelKey = tunnelMap.get(rs);
		if(null == tunnelKey) {
			synchronized (rs) {
                tunnelKey = tunnelMap.get(rs);
				if(null == tunnelKey) {
                    tunnelKey = createTunnel(rs);
					tunnelMap.put(rs, tunnelKey);
				}
			}
		}
		return tunnelKey;
	}

    /**
     * 产生新的连接
     * @param service
     * @return
     */
	private String createTunnel(RemoteService service) throws Exception {
	    //TODO 如果已经连接成功的服务出现永久性（或长期）离线状态，则会出现连接失败，那么客户端会不断尝试重连，应该重新从注册中心获取提供者

		//没有配置注册中心的远程服务，直连服务提供者主机
		if(null == service.getCenterId() || service.getClazz() == ServiceRegistryService.class) {
			logger.info("trying to connect directly to remote host.");
			try {
				InetSocketAddress address;
				if(service.getClazz() == ServiceRegistryService.class) {
					RegistryCenter center = RegistryFactory.getInstance().getRegistryCenter(service.getCenterId());
					address = new InetSocketAddress(center.getHost(), center.getPort());
				} else {
					address = new InetSocketAddress(service.getHost(), service.getPort());
				}
                String tunnelKey = ClientTunnel.initTunnel(service, address);
                return tunnelKey;
			} catch (IoSessionException e) {
				throw e;
			}
		} else {
			logger.debug("Trying to get provider from registry center.");
			ServiceConsumer consumer;
			CandidateProvider provider;
            consumer = new ServiceConsumer(service);
            provider = JProxyServiceRegistry.getInstance().getAvailableService(service, consumer);
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
						String tunnelKey = ClientTunnel.initTunnel(service, address);
						return tunnelKey;
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

    public static void removeTunnel(String key) {
        synchronized (tunnelMap) {
            for(Iterator<RemoteService> it = tunnelMap.keySet().iterator(); it.hasNext();) {
                String _key = tunnelMap.get(it.next());
                if(StringUtil.equals(key, _key)) {
                    it.remove();
                }
            }
        }
    }
    //END
}

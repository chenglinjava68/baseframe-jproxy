package com.hty.baseframe.jproxy.registry;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hty.baseframe.jproxy.bean.LocalService;
import com.hty.baseframe.jproxy.common.ServiceFactory;
import com.hty.baseframe.jproxy.registry.impl.JProxyServiceRegistry;
import com.hty.baseframe.jproxy.registry.loader.ServiceProvider;

/**
 * 服务提供者定时器，定时向注册中心注册服务
 * @author Tisnyi
 */
public class ServiceRegistryManager extends TimerTask {
	
	private final Log logger = LogFactory.getLog(getClass());
	
	private final Set<Integer> regsuccess = new HashSet<Integer>();
	
	
	@Override
	public void run() {
		//System.out.println("执行定时注册服务...");
		ServiceRegistryClient regCenterClient = JProxyServiceRegistry.getInstance();
		//暴露本地服务
		Map<Class<?>, Map<String, LocalService>> lses = ServiceFactory.getLocalServices();
		for(Iterator<Map<String, LocalService>> it = lses.values().iterator(); it.hasNext();) {
			Map<String, LocalService> verMap = it.next();
			if(null != verMap) {
				for(Iterator<LocalService> r = verMap.values().iterator(); r.hasNext();) {
					LocalService ls = r.next();
					if(regsuccess.contains(ls.hashCode())) {
						continue;
					}
					//过滤特殊服务和不向注册中心暴露的服务
					if(ls.getClazz() != ServiceRegistryServer.class && ls.getCenterId() != null) {
						ServiceProvider provider = new ServiceProvider(ls);
						try {
							regCenterClient.register(ls, provider);
							regsuccess.add(ls.hashCode());
							logger.info("register LocalService successfully: " + ls);
						} catch (Exception e) {
							logger.info("register LocalService failed: " + ls);
							e.printStackTrace();
							continue;
						}
					}
				}
			}
		}
		
	}
}

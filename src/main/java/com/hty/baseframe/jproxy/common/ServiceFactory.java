package com.hty.baseframe.jproxy.common;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hty.baseframe.jproxy.bean.LocalService;
import com.hty.baseframe.jproxy.bean.RemoteService;
import com.hty.baseframe.jproxy.client.ClientSocketManager;
import com.hty.baseframe.jproxy.client.ServiceInvocationHandler;
import com.hty.baseframe.jproxy.exception.IllegalConfigurationException;
import com.hty.baseframe.jproxy.exception.NoSuchServiceException;
import com.hty.baseframe.jproxy.registry.ServiceRegistryServer;
/**
 * 服务工厂，保存本地和远程服务的配置信息，产生代理服务类等
 * @author Tisnyi
 */
public class ServiceFactory {
	
	private static Log logger = LogFactory.getLog(ServiceFactory.class);
	//						<接口类, <版本, LocalService>>
	private static final Map<Class<?>, Map<String, LocalService>> local_services = 
			new HashMap<Class<?>, Map<String, LocalService>>();
	//						<接口类, <版本, LocalService>>
	private static final Map<Class<?>, Map<String, RemoteService>> remote_services = 
			new HashMap<Class<?>, Map<String, RemoteService>>();
	//					<接口类, <版本, 代理实例类>>
	private static Map<RemoteService, Object> proxyInstances = 
			new HashMap<RemoteService, Object>();
	
	private static BeanProvider beanProvider;
	
	/**
	 * 添加一个本地服务，该远程服务来自配置文件。
	 * @param localService
	 */
	public static synchronized void addLocalService(LocalService localService) {
		if(null == beanProvider) {
			throw new IllegalConfigurationException("No BeanProvider specific, can not add LocalService.");
		}
		
		if(null == localService) {
			throw new IllegalConfigurationException("Can not add a null LocalService!");
		}
		if(!localService.getClazz().isInterface()) {
			throw new IllegalConfigurationException("LocalService '"+ localService.getClazz().getName() +"' with version : '" + localService.getVersion() + "' is not an interface type.");
		}
		Map<String, LocalService> verMap = local_services.get(localService.getClazz());
		if(null == verMap) {
			verMap = new HashMap<String, LocalService>();
			local_services.put(localService.getClazz(), verMap);
		}
		if(null != verMap.get(localService.getVersion())) {
			throw new IllegalConfigurationException("LocalService '"+ localService.getClazz().getName() +"' with version : '" + localService.getVersion() + "' already exists.");
		}
		logger.info("Adding LocalService: " + localService);
		verMap.put(localService.getVersion(), localService);
	}
	/**
	 * 获取一个本地服务
	 * @param type 服务接口类
	 * @param version 服务版本
	 * @return LocalService
	 */
	public static LocalService getLocalService(Class<?> type, String version) {
		Map<String, LocalService> verMap = local_services.get(type);
		if(null == verMap) {
			throw new NoSuchServiceException("No LocalService of type " + type.getName() + " with version:"+ version);
		}
		//特殊处理，ServiceRegistryServer忽略版本
		if(type == ServiceRegistryServer.class) {
			LocalService ls = verMap.get(null);
			if(null == ls) {
				throw new NoSuchServiceException("No LocalService of type " + type.getName() + " with version:"+ version);
			}
			return ls;
		}
		if(null == verMap.get(version)) {
			throw new NoSuchServiceException("No LocalService of type " + type.getName() + " with version:"+ version);
		}
		return verMap.get(version);
	}
	
	
	/**
	 * 添加一个远程服务，该远程服务可来自配置文件，也可来自动态创建的远程服务。
	 * @param remoteService
	 */
	public static synchronized void addRemoteService(RemoteService remoteService) {
		if(null == remoteService) {
			throw new IllegalConfigurationException("Can not add a null RemoteService!");
		}
		if(!remoteService.getClazz().isInterface()) {
			throw new IllegalConfigurationException("RemoteService '"+ remoteService.getClazz().getName() +"' with version : '" + remoteService.getVersion() + "' is not an interface type.");
		}
		Map<String, RemoteService> verMap = remote_services.get(remoteService.getClazz());
		if(null == verMap) {
			verMap = new HashMap<String, RemoteService>();
			remote_services.put(remoteService.getClazz(), verMap);
		}
		if(null != verMap.get(remoteService.getVersion())) {
			throw new IllegalConfigurationException("RemoteService '"+ remoteService.getClazz().getName() +"' with version : '" + remoteService.getVersion() + "' already exists.");
		}
		logger.info("Adding RemoteService: " + remoteService);
		ClientSocketManager.getInstance().initSocketList(remoteService);
		verMap.put(remoteService.getVersion(), remoteService);
	}
	/**
	 * 获取远程服务
	 * @param id
	 * @return
	 */
	public static RemoteService getRemoteService(Class<?> type, String version) {
		Map<String, RemoteService> verMap = remote_services.get(type);
		if(null == verMap || null == verMap.get(version)) {
			throw new NoSuchServiceException("No RemoteService of type " + type.getName());
		}
		return verMap.get(version);
	}
	
	/**
	 * 获取远程服务代理实例（任何版本）
	 * @param interfaceClass 代理接口类
	 * @return
	 */
	public synchronized static <T> T getProxyInstance(Class<T> interfaceClass) {
		return getProxyInstance(interfaceClass, null);
	}
	/**
	 * 获取远程服务代理实例（指定版本）
	 * @param interfaceClass 代理接口类
	 * @param version 接口版本
	 * @return 代理接口对象
	 */
	@SuppressWarnings("unchecked")
	public synchronized static <T> T getProxyInstance(Class<T> interfaceClass, String version) {
		Object proxy;
		RemoteService rs = getRemoteService(interfaceClass, version);
		proxy = proxyInstances.get(rs);
		if(null == proxy) {
			proxy = Proxy.newProxyInstance(interfaceClass.getClassLoader(), 
					new Class<?>[]{interfaceClass}, new ServiceInvocationHandler(rs));
			proxyInstances.put(rs, proxy);
		}
		return (T) proxy;
	}
	
	public static int getLocalServiceCount() {
		return local_services.size();
	}
	
	public static int getRemoteServiceCount() {
		return remote_services.size();
	}
	public static BeanProvider getBeanProvider() {
		return beanProvider;
	}
	public static void setBeanProvider(BeanProvider beanProvider) {
		ServiceFactory.beanProvider = beanProvider;
	}
	public static Map<Class<?>, Map<String, LocalService>> getLocalServices() {
		return local_services;
	}
	public static Map<Class<?>, Map<String, RemoteService>> getRemoteServices() {
		return remote_services;
	}
}

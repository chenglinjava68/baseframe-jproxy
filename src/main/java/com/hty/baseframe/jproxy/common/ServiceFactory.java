package com.hty.baseframe.jproxy.common;

import com.hty.baseframe.common.util.StringUtil;
import com.hty.baseframe.jproxy.bean.LocalService;
import com.hty.baseframe.jproxy.bean.RemoteService;
import com.hty.baseframe.jproxy.client.ServiceInvocationHandler;
import com.hty.baseframe.jproxy.exception.IllegalConfigurationException;
import com.hty.baseframe.jproxy.exception.NoSuchServiceException;
import com.hty.baseframe.jproxy.util.ConditionMatchUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 服务工厂，保存本地和远程服务的配置信息，产生代理服务类等
 * @author Tisnyi
 */
public class ServiceFactory {
	
	private static Log logger = LogFactory.getLog(ServiceFactory.class);

	//本地服务 <接口类, List<LocalService>>
	private static final Map<Class<?>, List<LocalService>> local_services =
			new HashMap<Class<?>, List<LocalService>>();
	//远程服务 <接口类, List<RemoteService>>
	private static final Map<Class<?>, List<RemoteService>> remote_services =
			new HashMap<Class<?>, List<RemoteService>>();
	//远程服务代理映射 <接口类, <版本, 代理实例类>>
	private static Map<RemoteService, Object> proxyInstances = new HashMap<RemoteService, Object>();

	/*Bean提供工具类*/
	private static BeanProvider beanProvider;
	
	/**
	 * 添加一个本地服务，该远程服务来自配置文件。
	 * @param localService
	 */
	public static synchronized void addLocalService(LocalService localService) {

		if(null == localService) {
			throw new IllegalConfigurationException("Can not add a null LocalService!");
		}
		if(!localService.getClazz().isInterface()) {
			throw new IllegalConfigurationException("LocalService '"+ localService.getClazz().getName() +"' is not an interface.");
		}
		List<LocalService> list = local_services.get(localService.getClazz());
		if(null == list) {
			list = new ArrayList<LocalService>();
			local_services.put(localService.getClazz(), list);
		}
		if(list.isEmpty()) {
			logger.info("Add LocalService with condition: " + localService.getConditions());
			list.add(localService);
		}
		else
		for (int i = 0; i < list.size(); i++) {
			LocalService ls = list.get(i);
			int cmp = ConditionMatchUtil.mapCompare(ls.getConditions(), localService.getConditions());
			if(cmp == 0 || cmp == 1) {
				//已存在相同条件的LocalService,不再添加
				logger.warn("LocalService is already exists with condition: " + localService.getConditions());
				continue;
			}
			else if (cmp == 2) {
				//新加入的LocalService条件包含当前LocalService，替换
				logger.info("LocalService replaced with condition: " + localService.getConditions());
				list.set(i, localService);
			}
			else {
				logger.info("Add LocalService with condition: " + localService.getConditions());
				list.add(localService);
			}
		}
	}
	/**
	 * 获取一个本地服务
	 * @param type 服务接口类
	 * @param conditions 服务版本
	 * @return LocalService
	 */
	public static LocalService getLocalService(Class<?> type, Map<String, String> conditions) {
		List<LocalService> localServices = local_services.get(type);
		if(null == localServices || localServices.isEmpty()) {
			throw new NoSuchServiceException("No LocalService found of type " + type.getName());
		}
		for (LocalService ls : localServices) {
			if(ConditionMatchUtil.isMatch(conditions, ls.getConditions())) {
				return ls;
			}
		}
		throw new NoSuchServiceException("No LocalService of type " + type.getName());
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
			throw new IllegalConfigurationException("RemoteService '"+ remoteService.getClazz().getName() +"' is not an interface.");
		}
		List<RemoteService> list = remote_services.get(remoteService.getClazz());
		if(null == list) {
			list = new ArrayList<RemoteService>();
			remote_services.put(remoteService.getClazz(), list);
		}
		if(list.isEmpty()) {
			logger.info("Add RemoteService with condition: " + remoteService.getConditions());
			list.add(remoteService);
		}
		else
		for (int i = 0; i < list.size(); i++) {
			RemoteService ls = list.get(i);
			int cmp = ConditionMatchUtil.mapCompare(ls.getConditions(), remoteService.getConditions());
			if(cmp == 0 || cmp == 1) {
				//已存在相同条件的RemoteService,不再添加
				logger.warn("RemoteService is already exists with condition: " + remoteService.getConditions());
				continue;
			}
			else if (cmp == 2) {
				//新加入的RemoteService条件包含当前RemoteService，替换
				logger.info("RemoteService replaced with condition: " + remoteService.getConditions());
				list.set(i, remoteService);
			}
			else {
				logger.info("Add RemoteService with condition: " + remoteService.getConditions());
				list.add(remoteService);
			}
		}
	}

	/**
	 * 获取远程服务
	 * @param type 接口类
	 * @param conditions 条件
	 * @return
	 */
	public static RemoteService getRemoteService(Class<?> type, String registryCenterId,
												 Map<String, String> conditions) {
		List<RemoteService> remoteServices = remote_services.get(type);
		if(null == remoteServices || remoteServices.isEmpty()) {
			throw new NoSuchServiceException("No RemoteService found of type " + type.getName());
		}
		for (RemoteService ls : remoteServices) {
			if(ConditionMatchUtil.isMatch(conditions, ls.getConditions())) {
				if(!StringUtil.isEmpty(registryCenterId)) {
					if(!StringUtil.equals(registryCenterId, ls.getCenterId())) {
						continue;
					}
				}
				return ls;
			}
		}
		throw new NoSuchServiceException("No RemoteService of type " + type.getName());
	}
	
	/**
	 * 获取远程服务代理实例
	 * @param interfaceClass 代理接口类
	 * @return
	 */
	public synchronized static <T> T getProxyInstance(Class<T> interfaceClass) {
		return getProxyInstance(interfaceClass, null, null);
	}
	/**
	 * 获取远程服务代理实例
	 * @param interfaceClass 代理接口类
	 * @return
	 */
	public synchronized static <T> T getProxyInstance(Class<T> interfaceClass,
										  Map<String, String> conditions) {
		return getProxyInstance(interfaceClass, null, conditions);
	}
	/**
	 * 获取远程服务代理实例
	 * @param interfaceClass 代理接口类
	 * @param registryCenterId 注册中心ID
	 * @return 代理接口对象
	 */
	@SuppressWarnings("unchecked")
	public synchronized static <T> T getProxyInstance(Class<T> interfaceClass, String registryCenterId,
						Map<String, String> conditions) {
		Object proxy;
		RemoteService rs = getRemoteService(interfaceClass, registryCenterId, conditions);
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
	public static Map<Class<?>, List<LocalService>> getLocalServices() {
		return local_services;
	}
	public static Map<Class<?>, List<RemoteService>> getRemoteServices() {
		return remote_services;
	}
}

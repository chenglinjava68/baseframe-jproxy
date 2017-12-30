package com.hty.baseframe.jproxy.common;

import com.hty.baseframe.jproxy.bean.RegistryCenter;
import com.hty.baseframe.jproxy.bean.RemoteService;
import com.hty.baseframe.jproxy.exception.IllegalConfigurationException;
import com.hty.baseframe.jproxy.registry.ServiceRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 注册中心工厂类
 *
 * @author Hetianyi 2017/12/30
 * @version 1.0
 */
public class RegistryFactory {

    private static final Logger logger = LoggerFactory.getLogger(RegistryFactory.class);
    /**
     * 单实例
     */
    private static RegistryFactory registryFactory;
    /**
     * 注册中心
     */
    private static Map<String, RegistryCenter> centers = new HashMap<String, RegistryCenter>();

    /**
     * 构造方法私有化
     */
    private RegistryFactory() {
    }

    /**
     * 获取单实例的RegistryFactory
     *
     * @return RegistryFactory
     */
    public static synchronized RegistryFactory getInstance() {
        if (null == registryFactory) {
            registryFactory = new RegistryFactory();
        }
        return registryFactory;
    }

    /**
     * 添加一个注册中心
     *
     * @param center 注册中心Bean
     */
    public synchronized void addRegistryCenter(RegistryCenter center) {
        if (null == center) {
            throw new IllegalConfigurationException("Can not add a null RegistryCenter.");
        }
        if (!centers.containsKey(center.getId())) {
            logger.info("Adding RegistryCenter: " + center);
            //没添加一个注册中心，自动添加一个特殊的RemoteService（注册服务）
            //注册服务最多只给一个socket连接，且token，version，centerId均为空
            RemoteService rs = new RemoteService(ServiceRegistryService.class,
                    center.getHost(), String.valueOf(center.getPort()), center.getId());
            ServiceFactory.addRemoteService(rs);
            centers.put(center.getId(), center);
        } else {
            logger.error("RegistryCenter with id : {} already exists, will not add.", center.getId());
        }
    }

    /**
     * 获取注册中心
     *
     * @param id 注册中心ID
     * @return
     */
    public RegistryCenter getRegistryCenter(String id) {
        return centers.get(id);
    }

    /**
     * 通常系统只有一个注册中心，如果只有一个注册中心，获取之
     *
     * @return
     */
    public synchronized static String uniqueCenterId() {
        if (!centers.isEmpty() && centers.size() == 1) {
            return centers.keySet().iterator().next();
        }
        return null;
    }
}

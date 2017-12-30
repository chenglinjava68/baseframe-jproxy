package com.hty.baseframe.jproxy.registry;

import com.hty.baseframe.jproxy.bean.LocalService;
import com.hty.baseframe.jproxy.common.ServiceFactory;
import com.hty.baseframe.jproxy.registry.impl.JProxyServiceRegistry;
import com.hty.baseframe.jproxy.registry.loader.ServiceProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

/**
 * 服务提供者定时器，定时向注册中心注册服务
 *
 * @author Hetianyi 2017/12/30
 * @version 1.0
 */
public class ServiceRegistryManager extends TimerTask {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistryManager.class);

    @Override
    public void run() {
        //System.out.println("执行定时注册服务...");
        ServiceRegistryClient regCenterClient = JProxyServiceRegistry.getInstance();
        //暴露本地服务
        Map<Class<?>, List<LocalService>> lses = ServiceFactory.getLocalServices();
        for (Iterator<List<LocalService>> it = lses.values().iterator(); it.hasNext(); ) {
            List<LocalService> list = it.next();
            if (null != list) {
                for (LocalService ls : list) {
                    //过滤特殊服务和不向注册中心暴露的服务
                    if (ls.getClazz() != ServiceRegistryService.class && ls.getCenterId() != null) {
                        ServiceProvider provider = new ServiceProvider(ls);
                        try {
                            regCenterClient.register(ls, provider);
                            logger.debug("register LocalService successfully: {}", ls);
                        } catch (Exception e) {
                            logger.error("register LocalService failed: {}", ls);
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }
}

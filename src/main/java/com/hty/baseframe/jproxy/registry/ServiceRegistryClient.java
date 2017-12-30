package com.hty.baseframe.jproxy.registry;

import com.hty.baseframe.jproxy.bean.LocalService;
import com.hty.baseframe.jproxy.bean.RemoteService;
import com.hty.baseframe.jproxy.registry.loader.CandidateProvider;
import com.hty.baseframe.jproxy.registry.loader.ServiceConsumer;
import com.hty.baseframe.jproxy.registry.loader.ServiceProvider;

/**
 * 注册中心客户端，和注册中心交互的接口
 *
 * @author Hetianyi 2017/12/30
 * @version 1.0
 */
public interface ServiceRegistryClient {
    /**
     * 服务提供者向注册中心注册服务
     *
     * @return
     */
    boolean register(LocalService ls, ServiceProvider provider);

    /**
     * 向注册中心获取可用的远程服务，本地实现将会缓存相同参数下上一次的Provider，直到Provider不可用
     *
     * @return
     */
    CandidateProvider getAvailableService(RemoteService rs, ServiceConsumer consumer);
}

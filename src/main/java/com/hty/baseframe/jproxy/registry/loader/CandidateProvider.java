package com.hty.baseframe.jproxy.registry.loader;

import java.io.Serializable;
import java.util.Set;

/**
 * 注册中心返回给消费者的数据载体，表示了服务提供者的基本信息
 *
 * @author Hetianyi 2017/12/30
 * @version 1.0
 */
public class CandidateProvider implements Serializable {

    private static final long serialVersionUID = -6125933296883661498L;

    /**
     * 服务接口类
     */
    private String clazz;
    /**
     * 服务提供者网络IP地址列表，可能存在多个地址
     */
    private Set<String> addresses;
    /**
     * 服务提供版本
     */
    private String version;
    /**
     * 服务端口
     */
    private int port;
    /**
     * 此参数由注册中心设置
     */
    private String providerLookBackAddress;
    /**
     * 此参数由注册中心设置, 消费者的IP地址
     */
    private String consumerLookBackAddress;


    public Set<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(Set<String> addresses) {
        this.addresses = addresses;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProviderLookBackAddress() {
        return providerLookBackAddress;
    }

    public void setProviderLookBackAddress(String providerLookBackAddress) {
        this.providerLookBackAddress = providerLookBackAddress;
    }

    public String getConsumerLookBackAddress() {
        return consumerLookBackAddress;
    }

    public void setConsumerLookBackAddress(String consumerLookBackAddress) {
        this.consumerLookBackAddress = consumerLookBackAddress;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    @Override
    public String toString() {
        return "{class: '" + clazz + "', version: '" + version
                + "', port: '" + port + "', addresses: '" + addresses
                + "', providerLookBackAddress: '" + providerLookBackAddress
                + "', consumerLookBackAddress: '" + consumerLookBackAddress + "'}";
    }
}

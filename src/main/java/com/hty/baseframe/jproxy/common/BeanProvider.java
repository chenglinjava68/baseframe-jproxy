package com.hty.baseframe.jproxy.common;

/**
 * 实现该接口并提供所需类的获取方式，
 * jproxy可能使用在各种环境下，不同环境下服务接口类的实例化方式不同，
 * 因此，jproxy不关心实现类是什么，只要提供该接口的实现类，
 * 通过该接口的实现类能获取本地服务接口的实现类即可。
 * 注意：如果配置了本地对外的服务（LocalService），需要实现该接口。
 *
 * @author Hetianyi 2017/12/30
 * @version 1.0
 */
public interface BeanProvider {
    /**
     * 根据类和版本字符获取服务实现类
     *
     * @param type 接口类
     */
    Object getBean(Class<?> type);
}

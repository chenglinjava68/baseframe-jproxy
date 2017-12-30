package com.hty.baseframe.jproxy.server;


import org.apache.mina.core.session.IoSession;

/**
 * SocketListener附加在每个ServerSocketManager线程里，监听客户端Socket连接状态，如果客户端Socket断开，
 * 服务端应该能立刻感知到这个状态，此时ServerSocketManager会通知绑定到自己的SocketListener客户socket已断开
 * 并作出相应的操作，如删除该客户端的所有服务提供者。
 * 此类在系统启动时传入ServiceServer，并由其传播。
 *
 * @author Hetianyi 2017/12/30
 * @version 1.0
 */
public interface SocketListener {
    /**
     * 通知有新的socket连接传入
     */
    void connect(IoSession session);

    /**
     * 通知有客户端socket断开
     */
    void disconnect(IoSession session);
}

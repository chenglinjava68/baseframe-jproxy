package com.hty.baseframe.jproxy.server;

import com.hty.baseframe.jproxy.common.ServiceFactory;
import com.hty.baseframe.jproxy.common.SysProperties;
import com.hty.baseframe.jproxy.tunel.server.ServerTunnel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 服务提供者服务端，在指定端口接收消费者的socket连接。
 * 只有配置了LocalService才会启动服务端。
 *
 * @author Hetianyi 2017/12/30
 * @version 1.0
 */
public final class ServiceServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ServiceServer.class);
    /**
     * 本地服务端口
     */
    private int listen_port;
    /**
     * 客户端最大总连接数
     */
    private int max_conn_count;
    /**
     * 客户端当前连接总数
     */
    private static int client_count = 0;


    private SocketListener listener;

    public ServiceServer(SocketListener listener) throws IOException {
        listen_port = Integer.valueOf(SysProperties.getProperty("local_service_port"));
        try {
            max_conn_count = Integer.valueOf(SysProperties.getProperty("max_connection"));
        } catch (NumberFormatException e) {
            max_conn_count = 100;
        }
        this.listener = listener;
    }

    public void run() {

        while (ServiceFactory.getLocalServiceCount() == 0) {
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        new ServerTunnel(listen_port, listener);
        logger.info("Starting JProxy Server : [max_connection={}]", max_conn_count);
        logger.info("JProxy Server is listening on port : [{}]", listen_port);

    }

    synchronized void flagConnection(int flag) {
        if (flag > 0) {
            client_count++;
        } else {
            client_count--;
        }
    }
}

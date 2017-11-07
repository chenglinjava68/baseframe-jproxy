package com.hty.baseframe.jproxy.server;

import com.hty.baseframe.jproxy.common.ServiceFactory;
import com.hty.baseframe.jproxy.common.SysProprties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 服务提供者服务端，在指定端口接收消费者的socket连接。
 * 只有配置了LocalService才会启动服务端。
 * @author Tisnyi
 */
public final class ServiceServer implements Runnable {
	
	private final Log logger = LogFactory.getLog(ServiceServer.class);
	/** 本地服务端口 */
	private int listen_port;
	/** 客户端最大总连接数 */
	private int max_conn_count;
	/** 客户端当前连接总数 */
	private static int client_count = 0;
	
	
	private ServerSocket ss;
	
	private SocketListener listener;
	
	public ServiceServer(SocketListener listener) throws IOException {
		listen_port = Integer.valueOf(SysProprties.getProperty("local_service_port"));
		try {
			max_conn_count = Integer.valueOf(SysProprties.getProperty("max_connection"));
		} catch (NumberFormatException e) {
			max_conn_count = 100;
		}
		this.listener = listener;
	}
	
	public void run() {

		while(ServiceFactory.getLocalServiceCount() == 0) {
			try {
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			ss = new ServerSocket(listen_port);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		logger.info("Starting JProxy Server : [max_connection="+ max_conn_count +"]");
		logger.info("JProxy Server is listening on port : [" + listen_port + "]");
		while(true) {
			try {
				//限制客户端连接数
				while(client_count > max_conn_count) {
					try {
					Thread.sleep(1000);
					} catch (InterruptedException e) {}
				}
				Socket client = ss.accept();
				ServerSocketManager manager = new ServerSocketManager(client, this, listener);
				flagConnection(1);
				Thread t = new Thread(manager);
				t.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	synchronized void flagConnection(int flag) {
		if(flag > 0) {
			client_count++;
		} else {
			client_count--;
		}
	}
}

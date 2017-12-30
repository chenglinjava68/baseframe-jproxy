package com.hty.baseframe.jproxy;

import com.hty.baseframe.jproxy.registry.ServiceRegistryManager;
import com.hty.baseframe.jproxy.server.ServiceServer;
import com.hty.baseframe.jproxy.server.SocketListener;
import com.hty.baseframe.jproxy.util.ConfigLoader;

import java.util.Timer;

/**
 * ==========================<br>
 * | JProxy<br>
 * | <i style='font-size:0.9em'>Powered by Tisnyi</i><br>
 * | Email:<a href="mailto:hehety@outlook.com">hehety@outlook.com</a><br>
 * ==========================<br>
 * 1.什么是JProxy？<br>
 * &nbsp;&nbsp;&nbsp;JProxy是一个远程调用插件，基于Java的RMI，序列化机制，
 * 集成了Google的序列化工具protostuff使得序列化效率提升巨大。<br>
 * 功能类似dubbo，但是，JProxy多了可以通过条件筛选来动态选择服务提供者。
 * 2.JProxy能做什么？<br>
 * &nbsp;&nbsp;&nbsp;JProxy能够代替Java原生的RMI远程调用，但是更加灵活，无需Web中间件的支持，能够调用任意服务端支持的开放接口（本地无需实现，只需要接口类即可），同时效率和速度更高。<br>
 * &nbsp;&nbsp;&nbsp;JProxy启动类，在集成进应用时，需要开发者以各自的方式调用<br>
 * <pre>
 * JProxy.start();
 * </pre>
 * 来启动JProxy服务。
 *
 * @author Hetianyi 2017/12/30
 * @version 1.0
 */
public class JProxy {

	protected void printWelcome() {
		System.out.println("\n+-------------------------------------------------------+\n" + 
				"| * JProxy v1.0                                         |\n" +
				"| * Author:Hetianyi                                     |\n" +
				"| * Github:https://github.com/hetianyi/baseframe-jproxy |\n" +
				"+-------------------------------------------------------+");
	}
	/**
	 * 此方式专为router使用
	 */
	public void start1(SocketListener listener) throws Exception {
        printWelcome();
		ServiceServer server = new ServiceServer(listener);
		Thread t_server = new Thread(server);
		t_server.start();
	}
	/**
	 * 启动JProxy
	 */
	public void start(String conf) throws Exception {
		printWelcome();
		new ConfigLoader().load(conf);
		ServiceServer server = new ServiceServer(null);
		Thread t_server = new Thread(server);
		t_server.start();
		
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new ServiceRegistryManager(), 1000, 30000);
	}
}

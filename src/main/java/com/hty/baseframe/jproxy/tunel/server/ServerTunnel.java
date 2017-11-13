package com.hty.baseframe.jproxy.tunel.server;

import com.hty.baseframe.jproxy.server.SocketListener;
import com.hty.baseframe.jproxy.tunel.common.ServiceProtocolCodecFactory;
import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerTunnel {

	private final int port;

	private SocketListener listener;

	public ServerTunnel(int port, SocketListener listener) {
		this.port = port;
		this.listener = listener;
		startTunnel();
	}

	private final Logger logger = Logger.getLogger(ServerTunnel.class);

	public void startTunnel() {
		IoAcceptor acceptor;
		ExecutorService executor = Executors.newFixedThreadPool(1000);
		try {
			acceptor = new NioSocketAcceptor();
			// 设置链接超时时间
			acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ServiceProtocolCodecFactory()));
			// 添加过滤器(此项会导致内存急剧上升)
//			acceptor.getFilterChain().addLast("executor", new ExecutorFilter(executor));
			// 获得IoSessionConfig对象
			IoSessionConfig cfg = acceptor.getSessionConfig();
			// 读写通道10秒内无操作进入空闲状态
			cfg.setIdleTime(IdleStatus.BOTH_IDLE, 10);

			// 绑定逻辑处理器
			acceptor.setHandler(new ServerTunnelHandler(listener));
			// 绑定端口
			acceptor.bind(new InetSocketAddress(port));
			
			logger.info("Server started success at port: " + port);
		} catch (IOException e) {
			logger.error("Server started failed: ", e);
            e.printStackTrace();
		}
	}

}

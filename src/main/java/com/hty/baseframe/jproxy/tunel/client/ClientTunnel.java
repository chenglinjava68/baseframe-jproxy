package com.hty.baseframe.jproxy.tunel.client;

import com.hty.baseframe.jproxy.bean.ServiceRequest;
import com.hty.baseframe.jproxy.bean.ServiceResponse;
import com.hty.baseframe.jproxy.exception.IoSessionException;
import com.hty.baseframe.jproxy.tunel.common.ServiceProtocolCodecFactory;
import com.hty.baseframe.jproxy.util.NetWorkInterfaceUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.util.*;

public class ClientTunnel implements Runnable {
	
	private static final Log logger = LogFactory.getLog(ClientTunnel.class);

	private InetSocketAddress address = null;
	
	private IoSession session;

	// 创建一个非阻塞的客户端程序
	private static final NioSocketConnector connector = new NioSocketConnector(); // 创建连接
	static {
//		ExecutorService executor = Executors.newFixedThreadPool(1000);
		// 设置链接超时时间
		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ServiceProtocolCodecFactory()));
		// 添加过滤器
//			connector.getFilterChain().addLast("executor", new ExecutorFilter(executor));
		// 添加业务逻辑处理器类
		connector.setHandler(new ClientTunnelHandler());// 添加业务处理
	}

	/**
	 * 将每个Thread的写请求生成一个UUID进行映射，请求开始从该集合注册， 结束从该集合移除
	 * 并返回处理结果
	 */
	private static final Map<String, Object> requestMapping =
			Collections.synchronizedMap(new HashMap<String, Object>());

	private static final Map<String, IoSession> sessionAddressMap =
			Collections.synchronizedMap(new HashMap<String, IoSession>());

	public ClientTunnel(InetSocketAddress address) throws IoSessionException {
		this.address = address;
        synchronized (sessionAddressMap) {
            if(sessionAddressMap.containsKey(address.getAddress().getHostAddress() + ":" + address.getPort())) {
                //说明当前服务创建的连接已经和其他服务创建的连接重复，可以复用
                logger.info("Same connection exists so will not to create a new connection: " + address.toString());
            } else {
                if(!startTunnel()) {
                    throw new IoSessionException("Cannot connect to server!");
                }
            }
        }
	}
	
	private boolean startTunnel() {
		if(!NetWorkInterfaceUtil.hostReachale(address.getAddress().getHostAddress())) {
			return false;
		}

		//设置监听器，断开自动重连
		connector.addListener(new ClientTunnelIoListener() {
			@Override
			public void sessionDestroyed(IoSession old)
					throws Exception {
				logger.error("Session disconnect! trying to reconnect.");
                synchronized (sessionAddressMap) {
                    sessionAddressMap.remove(address.getAddress().getHostAddress() + ":" + address.getPort());
                }
				for(;;) {
					try {
						if(old.getId() == session.getId()) {
							ConnectFuture future = connector.connect(address);// 创建连接
							future.awaitUninterruptibly();// 等待连接创建成功
							session = future.getSession();// 获取会话
							if (session.isConnected()) {
                                synchronized (sessionAddressMap) {
                                    sessionAddressMap.put(address.getAddress().getHostAddress() + ":" + address.getPort(), session);
                                }
								//唤醒所有在当前session上等待的线程
								synchronized (requestMapping) {
									for (Map.Entry<String, Object> entry : requestMapping.entrySet()) {
										if(entry.getKey().startsWith(old.getId()+":")) {
											ThreadWaitResultLockUtil.notifyThread(entry.getValue(), null, new IoSessionException("Connection destroyed!"));
										}
									}
								}
								session.setAttribute("requestMapping", requestMapping);
								session.setAttribute("side", "client");
								logger.info("Reconnection to server success!");
								break;
							}
						} else {
							break;
						}
					} catch (Exception e) {
						logger.error("Reconnection to server failed: " + e.getMessage()); 
						Thread.sleep(10000);
					}
				}
			}
		});

		//首次连接
		for(;;) {
			try {
				ConnectFuture future = connector.connect(address);// 创建连接
				future.awaitUninterruptibly();// 等待连接创建成功  
				session = future.getSession();// 获取会话  
				if (session.isConnected()) {
				    synchronized (sessionAddressMap) {
                        sessionAddressMap.put(address.getAddress().getHostAddress() + ":" + address.getPort(), session);
                    }
					session.setAttribute("requestMapping", requestMapping);
					//告诉编码器session是服务端的session还是客户端的session
					session.setAttribute("side", "client");
					logger.info("Connection to server success!");
					return true;
				}
			} catch (Exception e) {
				logger.error("Connection to server failed: " + e.getMessage());
				return false;
			}
		}
//		session.getCloseFuture().awaitUninterruptibly();// 等待连接断开
	}

	/**
	 * 获取当前session
	 * @return
	 */
	public IoSession getSession() {
		if(null != session && session.isConnected()) {
			return session;
		} else {
			return null;
		}
	}

	/**
	 * Tunel当前是否就绪
	 * @return
	 */
	public boolean ready() {
		if(null != session && session.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 线程向tunnel写请求数据
	 * @param request
	 */
	public ServiceResponse write(ServiceRequest request) throws Exception {
		IoSession session = getSession();
		if(null != session) {
			String uuid = UUID.randomUUID().toString();
			Object lock = ThreadWaitResultLockUtil.getThreadLock(Thread.currentThread());
			synchronized (requestMapping) {
                requestMapping.put(session.getId() + ":" + uuid, lock);
            }
			request.setRequestId(uuid);
			session.write(request);
			ServiceResponse resp = (ServiceResponse) ThreadWaitResultLockUtil.waitThread();
			return resp;
		} else {
			throw new IoSessionException("Current Session is unavailable!");
		}

	}

	static Map<String, Object> getRequestMapping() {
		return requestMapping;
	}

	@Override
	public void run() {
	}


}

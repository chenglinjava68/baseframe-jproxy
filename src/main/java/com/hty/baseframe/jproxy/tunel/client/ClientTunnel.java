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

	// 创建一个非阻塞的客户端程序
	private static final NioSocketConnector connector = new NioSocketConnector(); // 创建连接
    /*对相同的远程主机端口创建连接的时候要加锁，否则会出现连接不唯一
    * 格式:<String, Object>
    *     <host:port, Object>
    * */
    private static final Map<String, Object> sameConnectionLocks =
            new HashMap<String, Object>();
    /**
	 * 将每个Thread的写请求生成一个UUID进行映射，请求开始从该集合注册， 结束从该集合移除
	 * 并返回处理结果
	 */
	private static final Map<String, Object> requestMapping =
			Collections.synchronizedMap(new HashMap<String, Object>());

	private static final Map<String, IoSession> sessionAddressMap =
			Collections.synchronizedMap(new HashMap<String, IoSession>());

    static {
        //ExecutorService executor = Executors.newFixedThreadPool(1000);
        // 设置链接超时时间
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ServiceProtocolCodecFactory()));
        // 添加过滤器
        //connector.getFilterChain().addLast("executor", new ExecutorFilter(executor));
        // 添加业务逻辑处理器类
        connector.setHandler(new ClientTunnelHandler());// 添加业务处理
    }

    private final InetSocketAddress address;


    private Object getAddressLock(String key) {
        synchronized (sameConnectionLocks) {
            Object lock = sameConnectionLocks.get(key);
            if(null == lock) {
                lock = new Object();
                sameConnectionLocks.put(key, lock);
            }
            return lock;
        }
    }

	public ClientTunnel(InetSocketAddress address) throws IoSessionException {
        this.address = address;
        String key = address.getAddress().getHostAddress() + ":" + address.getPort();
        //新建Tunnel时判断host和port，如果匹配到相同的session，则使用此session。
        if(sessionAddressMap.containsKey(key)) {
            //说明当前服务创建的连接已经和其他服务创建的连接重复，可以复用
            logger.info("Same connection exists so will not to create a new connection: " + address.toString());
        } else {
            Object lock = getAddressLock(key);
            //在相同地址端口上创建连接时加锁，防止创建多个连接
            synchronized (lock) {
                if(sessionAddressMap.containsKey(key)) {
                    //说明当前服务创建的连接已经和其他服务创建的连接重复，可以复用
                    logger.info("Same connection exists so will not to create a new connection: " + address.toString());
                } else if(!startTunnel(address)) {
                    throw new IoSessionException("Cannot connect to server!");
                }
            }
        }
	}

	private boolean startTunnel(final InetSocketAddress address) {
		if(!NetWorkInterfaceUtil.hostReachale(address.getAddress().getHostAddress())) {
			return false;
		}
        ClientTunnelIoListener futureListener = new ClientTunnelIoListener() {
            @Override
            public void sessionDestroyed(IoSession old) throws Exception {

                //TODO 重连的时候会出现创建多个连接现象
                String remoteIp = ((InetSocketAddress) old.getRemoteAddress()).getAddress().getHostAddress();
                int remotePort = ((InetSocketAddress) old.getRemoteAddress()).getPort();
                logger.error("Session with id["+ old.getId() +"] disconnect! trying to reconnect.");
                //将绑定该主机端口的Session释放
                synchronized (sessionAddressMap) {
                    logger.info("Removing session ["+ remoteIp + ":" + remotePort +"] " + old);
                    sessionAddressMap.put(remoteIp + ":" + remotePort, null);
                }
                //唤醒所有在当前session上等待的线程
                synchronized (requestMapping) {
                    for (Iterator<String> it = requestMapping.keySet().iterator(); it.hasNext();) {
                        String reqId = it.next();
                        if(reqId.startsWith(old.getId()+":")) {
                            ThreadWaitResultLockUtil.notifyThread(requestMapping.get(reqId), null, new IoSessionException("Connection destroyed!"));
                            it.remove();
                        }
                    }
                }
                for(;;) {
                    try {
                        ConnectFuture future = connector.connect(old.getRemoteAddress());// 创建连接
                        future.awaitUninterruptibly();// 等待连接创建成功
                        IoSession session = future.getSession();// 获取会话
                        if (session.isConnected()) {
                            synchronized (sessionAddressMap) {
                                sessionAddressMap.put(remoteIp + ":" + remotePort, session);
                            }
                            session.setAttribute("requestMapping", requestMapping);
                            session.setAttribute("side", "client");
                            logger.info("Reconnect to server success: " + address);
                            break;
                        }
                    } catch (Exception e) {
                        logger.error("Reconnect to server["+ address +"] failed: " + e.getMessage());
                        Thread.sleep(10000);
                    }
                }
            }
        };
		//设置监听器，断开自动重连
		connector.addListener(futureListener);

		//首次连接
		for(;;) {
			try {
				ConnectFuture future = connector.connect(address);// 创建连接
				future.awaitUninterruptibly();// 等待连接创建成功  
				IoSession session = future.getSession();// 获取会话
				if (session.isConnected()) {
				    synchronized (sessionAddressMap) {
                        sessionAddressMap.put(address.getAddress().getHostAddress() + ":" + address.getPort(), session);
                    }
					session.setAttribute("requestMapping", requestMapping);
					//告诉编码器session是服务端的session还是客户端的session
					session.setAttribute("side", "client");
					logger.info("Successfully connect to server: " + address.getAddress().getHostAddress() + ":" + address.getPort());
					return true;
				}
			} catch (Exception e) {
				logger.error("Connect to server["+ address +"] failed: " + e.getMessage());
				//连接失败，移除监听器
				connector.removeListener(futureListener);
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
        String key = this.address.getAddress().getHostAddress() + ":" + this.address.getPort();
        IoSession session = sessionAddressMap.get(key);
		if(null != session && session.isConnected()) {
			return session;
		} else {
			return null;
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

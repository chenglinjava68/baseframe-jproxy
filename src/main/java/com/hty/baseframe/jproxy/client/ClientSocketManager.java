package com.hty.baseframe.jproxy.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.service.TestService;

import com.hty.baseframe.jproxy.bean.RemoteService;
import com.hty.baseframe.jproxy.exception.ServiceInvokationException;
import com.hty.baseframe.jproxy.registry.impl.JProxyServiceRegistry;
import com.hty.baseframe.jproxy.registry.loader.CandidateProvider;
import com.hty.baseframe.jproxy.registry.loader.ServiceConsumer;
import com.hty.baseframe.jproxy.util.NetWorkInterfaceUtil;
/**
 * 客户端Socket管理类，控制每个远程服务的Socket连接状态，连接数，创建新的连接等。
 * 解决了多线程并发问题，保证在高密度请求下Socket连接不会泄漏。
 * @author Tisnyi
 */
public class ClientSocketManager {
	
	private final Log logger = LogFactory.getLog(getClass());
	/** 单实例 */
	private static ClientSocketManager clientSocketManager;
	
	/** 服务的Socket容器 */
	private final Map<RemoteService, List<Socket>> sockets = 
			Collections.synchronizedMap(new HashMap<RemoteService, List<Socket>>());
	
	//记录Socket状态	 <Socket  true:待使用，false，使用中>
	private final Map<Socket, Boolean> socketStates = 
			Collections.synchronizedMap(new HashMap<Socket, Boolean>());
	
	/** 构造方法私有化，实现单例 */
	private ClientSocketManager() {
	}
	/**
	 * 获取该类的单实例
	 * @return
	 */
	public static synchronized ClientSocketManager getInstance() {
		if(null == clientSocketManager) {
			clientSocketManager = new ClientSocketManager();
		}
		return clientSocketManager;
	}
	
	/**
	 * 获取远程服务的Socket
	 * @param remoteService
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Socket getServiceSocket(RemoteService remoteService) 
			throws IOException, InterruptedException {
		
		synchronized (remoteService) {
			while(true) {
				List<Socket> list = sockets.get(remoteService);
				if(list.size() == 0) {
					return createNewSocket(remoteService);
				}
				Socket spareSocket = (Socket) socket_list_action(remoteService, null, "getstate");
				if(null != spareSocket) {
					return spareSocket;
				}
				if(((Integer) socket_list_action(remoteService, null, "size")) < remoteService.getPoolsize()) {
					return createNewSocket(remoteService);
				} else {
					remoteService.wait();
					continue;
				}
			}
		}
	}
	
	/**
	 * 返还正常的Socket
	 * @param remoteService
	 * @param socket
	 */
	public void returnSocket(RemoteService remoteService, Socket socket) {
		changeSocketState(socket, true);
		synchronized (remoteService) {
			remoteService.notify();
		}
	}
	/**
	 * 返回损坏的Socket。
	 * Socket损坏后销毁，在需要的时候会创建新的Socket填补。
	 * @param remoteService
	 * @param socket
	 */
	public void returnBrokenSocket(RemoteService remoteService, Socket socket) {
		synchronized (socketStates) {
			socketStates.remove(socket);
		}
		socket_list_action(remoteService, socket, "remove");
		synchronized (remoteService) {
			remoteService.notify();
		}
	}
	/**
	 * 对Service的Socket列表进行同步操作
	 * @return
	 */
	private Object socket_list_action(RemoteService service, 
						Socket socket, String action) {
		List<Socket> list = sockets.get(service);
		synchronized (list) {
			if("remove".equals(action)) {
				for(Iterator<Socket> it = list.iterator(); it.hasNext();) {
					Socket next = it.next();
					if(next == socket) {
						it.remove();
						break;
					}
				}
				return null;
			} else if("add".equals(action)) {
				list.add(socket);
				return null;
			} else if("getstate".equals(action)) {
				for(Socket s : list) {
					if(socketStates.get(s)) {
						changeSocketState(s, false);
						return s;
					}
				}
				return null;
			} else if("size".equals(action)) {
				return list.size();
			}
			return null;
		}
	}
	
	//测试Socket创建计数
	public static int count = 0;

	private Socket createNewSocket(RemoteService service) throws IOException {
		logger.debug("Create socket connection for service:" + service);
		if(service.getClazz() == TestService.class) {
			count++;
		}
		//没有配置注册中心的远程服务，直连服务提供者主机
		if(null == service.getCenterId()) {
			logger.debug("trying to connect directly to remote host.");
			Socket s = new Socket();
			SocketAddress add = new InetSocketAddress(service.getHost(), service.getPort());
			s.setOOBInline(true);
			s.setKeepAlive(true);
			s.setTcpNoDelay(true);
			s.setSendBufferSize(1024);
			s.connect(add, 30000);
			addNewSocket(service, s);
			return s;
		} else {
			logger.debug("trying to get provider from trgistry.");
			ServiceConsumer consumer = new ServiceConsumer(service);
			CandidateProvider provider = JProxyServiceRegistry.getInstance().getAvailableService(service, consumer);
			logger.info("provider was successfully obtained: " + provider);
			if(null != provider) {
				Set<String> excludes = new HashSet<String>();
				while(true) {
					String candidate = NetWorkInterfaceUtil.pickHostCandidate(consumer, provider, excludes);
					logger.info("select candidate ip: " + candidate);
					if(null == candidate) {
						throw new ServiceInvokationException("No provider available for service: " + service.getClazz().getName());
					}
					if(NetWorkInterfaceUtil.hostReachale(candidate)) {
						try {
							logger.info("trying to connect to candidate host " + candidate + ":" + provider.getPort());
							Socket s = new Socket();
							SocketAddress add = new InetSocketAddress(candidate, provider.getPort());
							s.setOOBInline(true);
							s.setKeepAlive(true);
							s.setTcpNoDelay(true);
							s.setSendBufferSize(1024);
							s.connect(add, 30000);
							addNewSocket(service, s);
							logger.info("successfully connected to remote host: " + candidate);
							return s;
						} catch (Exception e) {
							e.printStackTrace();
							excludes.add(candidate);
							logger.info("candidate ip is not available: " + candidate);
							continue;
						}
					} else {
						logger.info("candidate ip is not available: " + candidate);
						excludes.add(candidate);
						continue;
					}
				}
			}
			throw new ServiceInvokationException("No provider for service: " + service.getClazz().getName());
		}
	}
	/**
	 * 将创建的新Socket添加到socket列表
	 * @param remoteService
	 * @param socket
	 */
	private synchronized void addNewSocket(RemoteService remoteService, Socket socket) {
		socket_list_action(remoteService, socket, "add");
		changeSocketState(socket, false);
	}
	/**
	 * 修改socket的占用状态
	 * @param socket
	 * @param state
	 */
	private void changeSocketState(Socket socket, boolean state) {
		synchronized (socketStates) {
			socketStates.put(socket, state);
		}
	}
	
	/**
	 * 在添加RemoteService的时候初始化sockets里面的LinkedList
	 */
	public synchronized void initSocketList(RemoteService remoteService) {
		if(null == sockets.get(remoteService)) {
			List<Socket> list = new LinkedList<Socket>();
			sockets.put(remoteService, list);
		}
	}
}

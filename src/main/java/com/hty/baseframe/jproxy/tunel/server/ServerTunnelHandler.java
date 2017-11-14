package com.hty.baseframe.jproxy.tunel.server;

import com.hty.baseframe.common.util.StringUtil;
import com.hty.baseframe.jproxy.bean.LocalService;
import com.hty.baseframe.jproxy.bean.MethodEntity;
import com.hty.baseframe.jproxy.bean.ServiceRequest;
import com.hty.baseframe.jproxy.bean.ServiceResponse;
import com.hty.baseframe.jproxy.common.BeanProvider;
import com.hty.baseframe.jproxy.common.ServiceFactory;
import com.hty.baseframe.jproxy.server.SocketListener;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServerTunnelHandler extends IoHandlerAdapter {
	
	private static final Log logger = LogFactory.getLog(ServerTunnelHandler.class);

	private SocketListener listener;

	private static final ThreadLocal<IoSession> threadLocalSession = new ThreadLocal<IoSession>();


	public ServerTunnelHandler(SocketListener listener) {
		this.listener = listener;
	}

	private static Map<Long, Long> sessionIdleTimes = new HashMap<Long, Long>();


	@Override
	public void sessionCreated(IoSession session) throws Exception {
		sessionIdleTimes.put(session.getId(), 0L);
		if(null != listener) {
			listener.connect(session);
		}
		super.sessionCreated(session);
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		logger.info("session["+ session.getId() +"] idle.");
		//当连接进入空闲，10s调用该方法一次，当持续空闲一段时间，服务端主动断开连接，
		//bug：允许一段时间后
		//NioSocketAcceptor线程会出现cpu飙高的现象
		Long idleTimes = sessionIdleTimes.get(session.getId());
		if(idleTimes == null) {
			idleTimes = 0L;
		}
		idleTimes++;
		if(idleTimes > 30) {
			logger.warn("session closed by server: idle too long.");
			session.close(true);
		}
		sessionIdleTimes.put(session.getId(), idleTimes);
	}

	public static IoSession getCurrentIoSession() {
		return threadLocalSession.get();
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		logger.debug("Server send success.");
		sessionIdleTimes.put(session.getId(), 0L);
	}
	
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		logger.debug("Server closed.");
		sessionIdleTimes.remove(session.getId());
		if(null != listener) {
			listener.disconnect(session);
		}
	}
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		logger.debug("Server error: " + cause.getMessage());
		cause.printStackTrace();
	}
	
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		threadLocalSession.set(session);
		logger.debug("["+ session.getId() +"]Server received: " + message);
		ServiceRequest request = (ServiceRequest) message;
		String uuid = request.getRequestId();
		//检查调用权限
		int status = accessCheck(request, session);
		if(status == 1) {
			ServiceResponse resp = invokeServiceMethod(request);
			resp.setRequestId(uuid);
			writeResponse(resp, session, false);
		} else {
			ServiceResponse resp = new ServiceResponse();
			resp.setRequestId(uuid);
			resp.setCode(status);
			writeResponse(resp, session, false);
		}
	}

	/**
	 * 将响应写回到客户端
	 */
	private void writeResponse(ServiceResponse resp, IoSession session, boolean shutdown) {
		session.write(resp);
		if(shutdown) {
			logger.warn("Socket will close by server.");
			session.close(false);
		}
	}

	/**
	 * 调用目标服务的方法
	 * @param request
	 * @return
	 */
	private ServiceResponse invokeServiceMethod(ServiceRequest request) {
		ServiceResponse resp = new ServiceResponse();
		try {
			Class<?> clazz = request.getClazz();
			MethodEntity me = request.getMethodEntity();
			LocalService localService = ServiceFactory.getLocalService(clazz, null);
			BeanProvider provider = ServiceFactory.getBeanProvider();
			Object bizService = provider.getBean(localService.getClazz());
			Object ret = MethodUtils.invokeMethod(bizService, me.getMethodName(), me.getArgs(), me.getArgsTypes());
			resp.setCode(ServiceResponse.SUCCESS);
			resp.setResult(ret);
		} catch (Exception e) {
			e.printStackTrace();
			resp.setCode(ServiceResponse.INNER_ERROR);
			resp.setResult(e);
		}
		return resp;
	}
	/**
	 * 检查IP白名单和token
	 * @param request
	 * @return
	 */
	private int accessCheck(ServiceRequest request, IoSession session) {
		//检查IP名单
		Class<?> interfaceClass = request.getClazz();
		LocalService localService = ServiceFactory.getLocalService(interfaceClass, null);
		Object token = request.getParameter("token");
		Set<String> clients = localService.getClients();
		//System.out.println("clients="+clients);
		if(null != clients && !clients.isEmpty()) {
			//校验是否服务的白名单
			String[] parts;
			InetSocketAddress addr = (InetSocketAddress) session.getRemoteAddress();
			//System.out.println("client.getReuseAddress()=" + addr.getAddress().getHostAddress());
			parts = addr.getAddress().getHostAddress().split("\\.");
			if(null == parts || parts.length != 4)
				return ServiceResponse.PERMISSION_DENIE_IP;
			if(null != clients && !clients.isEmpty()){
				boolean matchone = false;
				for(String ip : clients){
					if("localhost".equals(ip))
						ip = "127.0.0.*";
					if(match(ip.split("\\."), parts)){
						matchone = true;
					}
				}
				if(!matchone) {
					return ServiceResponse.PERMISSION_DENIE_IP;
				}
			}
		}
		return checkToken(localService.getToken(), null == token ? "" : token.toString());
	}
	/**
	 * 匹配IP地址
	 * @param standard
	 * @param remote
	 * @return
	 */
	private boolean match(String[] standard, String[] remote){
		boolean ismatch = true;
		for(int i=0;i<4;i++){
			if("*".equals(standard[i]) || standard[i].equals(remote[i])){
				continue;
			}else{
				ismatch = false;
				break;
			}
		}
		return ismatch;
	}
	/**
	 * 校验请求token是否正确
	 * @param given 请求携带的token
	 * @param token 正确的token
	 * @return
	 */
	private int checkToken(String given, String token) {
		//service没有设置token，任何请求都允许
		if(StringUtil.isEmpty(token)) {
			return 1;
		}
		if(token.equals(given)) {
			return 1;
		}
		return ServiceResponse.PERMISSION_DENIE_TOKEN;
	}
}

package com.hty.baseframe.jproxy.tunel.server;

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

import java.util.HashMap;
import java.util.Map;

public class ServerTunnelHandler extends IoHandlerAdapter {
	
	private static final Log logger = LogFactory.getLog(ServerTunnelHandler.class);
	/**/
	private SocketListener listener;
	/*当前线程绑定IoSession*/
	private static final ThreadLocal<IoSession> threadLocalSession = new ThreadLocal<IoSession>();


	public ServerTunnelHandler(SocketListener listener) {
		this.listener = listener;
	}
	/*记录每个session连续空闲次数*/
	private static final Map<Long, Long> sessionIdleTimes = new HashMap<Long, Long>();


	@Override
	public void sessionCreated(IoSession session) throws Exception {
		logger.info("create session:" + session);
		sessionIdleTimes.put(session.getId(), 0L);
		if(null != listener) {
			listener.connect(session);
		}
		super.sessionCreated(session);
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		logger.debug("session["+ session.getId() +"] idle.");
		//当连接进入空闲，10s调用该方法一次，当持续空闲一段时间，服务端主动断开连接，
		//bug：允许一段时间后
		//NioSocketAcceptor线程会出现cpu飙高的现象
		Long idleTimes = sessionIdleTimes.get(session.getId());
		if(idleTimes == null) {
			idleTimes = 0L;
		}
		idleTimes++;
		/*if(idleTimes > 30) {
			logger.warn("session closed by server: idle too long.");
			session.close(true);
		}*/
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
		logger.debug("Session closed.");
		sessionIdleTimes.remove(session.getId());
		if(null != listener) {
			listener.disconnect(session);
		}
	}
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		logger.error(cause.getMessage());
		cause.printStackTrace();
	}
	
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		threadLocalSession.set(session);
		logger.debug("["+ session.getId() +"]Server received: " + message);
		ServiceRequest request = (ServiceRequest) message;
		String uuid = request.getRequestId();
		ServiceResponse resp = invokeServiceMethod(request);
		resp.setRequestId(uuid);
		writeResponse(resp, session, false);
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
}

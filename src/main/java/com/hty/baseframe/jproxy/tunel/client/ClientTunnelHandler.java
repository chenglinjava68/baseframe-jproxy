package com.hty.baseframe.jproxy.tunel.client;

import com.hty.baseframe.jproxy.bean.ServiceResponse;
import com.hty.baseframe.jproxy.exception.ServiceInvokationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import java.util.Map;

public class ClientTunnelHandler extends IoHandlerAdapter {
	
	private static final Log logger = LogFactory.getLog(ClientTunnelHandler.class);
	
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		logger.debug("Client send success.");
	}
	
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		logger.debug("Client closed.");
	}
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		logger.debug("Client error: " + cause.getMessage());
		cause.printStackTrace();
	}
	
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		logger.debug("Client received: " + message);
		ServiceResponse response = (ServiceResponse) message;
		Map<String, Object> requestMapping = ClientTunnel.getRequestMapping();
		if(null == response || null == response.getRequestId()) {
			throw new ServiceInvokationException("Null attribute 'requestId'!");
		}
		String uuid = response.getRequestId();
		Object lock = requestMapping.get(session.getId() + ":" + uuid);
		Thread t = ThreadWaitResultLockUtil.getThread(lock);
		if(null == t) {
			throw new ServiceInvokationException("No thread bound with lock: " + lock + " uuid:" + uuid);
		}
		requestMapping.remove(uuid);
		ThreadWaitResultLockUtil.notifyThread(lock, response, null);
	}
}

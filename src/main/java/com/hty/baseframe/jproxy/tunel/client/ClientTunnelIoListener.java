package com.hty.baseframe.jproxy.tunel.client;

import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public class ClientTunnelIoListener implements IoServiceListener {

	@Override
	public void serviceActivated(IoService service) throws Exception {
	}

	@Override
	public void serviceIdle(IoService service, IdleStatus idleStatus)
			throws Exception {
	}

	@Override
	public void serviceDeactivated(IoService service) throws Exception {
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
	}

	@Override
	public void sessionDestroyed(IoSession session) throws Exception {
	}

}

package com.hty.baseframe.jproxy.tunel.common;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

public class ServiceProtocolCodecFactory extends DemuxingProtocolCodecFactory {
	
	private ServiceDecoder decoder;
	
	private ServiceEncoder encoder;
	
	public ServiceProtocolCodecFactory() {
		decoder = new ServiceDecoder();
		encoder = new ServiceEncoder();
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		return this.decoder;
	}
	
	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		return encoder;
	}
	
	
}

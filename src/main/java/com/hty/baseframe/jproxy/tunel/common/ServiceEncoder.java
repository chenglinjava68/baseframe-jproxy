package com.hty.baseframe.jproxy.tunel.common;

import com.hty.baseframe.jproxy.common.Const;
import com.hty.baseframe.jproxy.util.SerializeUtil;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class ServiceEncoder extends ProtocolEncoderAdapter {

    @Override
    public void encode(IoSession session, Object message,
                       ProtocolEncoderOutput out) throws Exception {
        byte[] bs = SerializeUtil.serialize(message);
        byte[] head = SerializeUtil.getHeadString(bs.length, Const.REQUEST_HEAD_SIZE).getBytes();
        IoBuffer buf = IoBuffer.allocate(bs.length + head.length).setAutoExpand(true);
        buf.put(head);
        //添加一个字节标志，标识message来自Client还是Server
        buf.put(bs);
        buf.flip();
        out.write(buf);
    }

}

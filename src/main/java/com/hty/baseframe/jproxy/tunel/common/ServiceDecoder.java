package com.hty.baseframe.jproxy.tunel.common;

import com.hty.baseframe.jproxy.bean.ServiceRequest;
import com.hty.baseframe.jproxy.bean.ServiceResponse;
import com.hty.baseframe.jproxy.common.Const;
import com.hty.baseframe.jproxy.util.SerializeUtil;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.serialization.ObjectSerializationDecoder;

import java.io.IOException;
import java.io.InputStream;

public class ServiceDecoder extends ObjectSerializationDecoder {

    @Override
    public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out)
            throws Exception {

        Integer left = (Integer) session.getAttribute("left");
        if (null == left) {
            left = Const.REQUEST_HEAD_SIZE;
            session.setAttribute("left", left);
        }

        Boolean lefthead = (Boolean) session.getAttribute("lefthead");
        ;
        if (null == lefthead) {
            lefthead = true;
            session.setAttribute("lefthead", lefthead);
        }

        IoBuffer lastBuffer = (IoBuffer) session.getAttribute("lastBuffer");
        if (null == lastBuffer) {
            lastBuffer = IoBuffer.allocate(2048).setAutoExpand(true);
            session.setAttribute("lastBuffer", lastBuffer);
        }

        InputStream ips = in.asInputStream();
        byte[] bs = null;
        int len = 0;
        try {
            while (true) {
                if (lefthead) {
                    bs = new byte[left];
                    len = ips.read(bs);
                    if (len == -1) {
                        break;
                    }
                    lastBuffer.put(bs, 0, len);
                    if (len < left) {
                        left = left - len;
                        session.setAttribute("left", left);
                        continue;
                    } else {
                        byte[] headbs = new byte[Const.REQUEST_HEAD_SIZE];
                        int position = lastBuffer.position();
                        lastBuffer.flip();
                        try {
                            lastBuffer.get(headbs, 0, position);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        left = Integer.valueOf(new String(headbs));
                        lefthead = false;
                        lastBuffer.clear();

                        session.setAttribute("lastBuffer", lastBuffer);
                        session.setAttribute("lefthead", lefthead);
                        session.setAttribute("left", left);
                    }
                } else {
                    bs = new byte[left];
                    len = ips.read(bs);
                    if (len == -1) {
                        break;
                    }
                    lastBuffer.put(bs, 0, len);
                    if (len < left) {
                        left = left - len;
                        session.setAttribute("left", left);
                        continue;
                    } else {
                        byte[] headbs = new byte[lastBuffer.position()];
                        int position = lastBuffer.position();
                        lastBuffer.flip();
                        lastBuffer.get(headbs, 0, position);
                        left = Const.REQUEST_HEAD_SIZE;
                        lefthead = true;
                        lastBuffer.clear();

                        session.setAttribute("lastBuffer", lastBuffer);
                        session.setAttribute("lefthead", lefthead);
                        session.setAttribute("left", left);

                        String side = (String) session.getAttribute("side");
                        Object ret;
                        if ("client".equalsIgnoreCase(side)) {
                            ret = SerializeUtil.deserialize(headbs, ServiceResponse.class);
                        } else {
                            ret = SerializeUtil.deserialize(headbs, ServiceRequest.class);
                        }
                        out.write(ret);
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
            //如果出现数据错误，则关闭session
            session.close(false);
        }
    }
}

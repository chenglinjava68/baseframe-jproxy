package com.hty.baseframe.jproxy.client;

import com.hty.baseframe.jproxy.bean.MethodEntity;
import com.hty.baseframe.jproxy.bean.RemoteService;
import com.hty.baseframe.jproxy.bean.ServiceRequest;
import com.hty.baseframe.jproxy.bean.ServiceResponse;
import com.hty.baseframe.jproxy.common.Const;
import com.hty.baseframe.jproxy.util.MethodUtil;
import com.hty.baseframe.jproxy.util.SerializeUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.Socket;
/**
 * 本地调用远程接口的数据发送工具类。<br>
 * 发送请求到远程主机并返回远程处理结果。
 * @author Tisnyi
 */
public class ServiceClient {
	
	private final static Log logger = LogFactory.getLog(ServiceClient.class);
	
	/**
	 * 发送请求
	 * @param service RemoteService
	 * @param socket socket
	 * @param me MethodEntity
	 * @return
	 * @throws IOException
	 * @throws NotSerializableException
	 */
	static ServiceResponse sendRequest(RemoteService service, Socket socket, MethodEntity me) 
			throws IOException, NotSerializableException {
		if(!isArgsSerializabled(me.getArgs()))
			throw new NotSerializableException("arg(s) of method " + me.getClass().getName() + "."+ me.getMethodName() +"() is not Serializable.");
		ServiceRequest req = new ServiceRequest();
		req.setMethodEntity(me);
		req.setClazz(service.getClazz());
		req.addParameter("token", service.getToken());
		byte[] bs = SerializeUtil.serialize(req);
		String head = SerializeUtil.getHeadString(bs.length, Const.REQUEST_HEAD_SIZE);
		try {
			OutputStream ops = socket.getOutputStream();
			ops.write(head.getBytes("UTF-8"));
			ops.write(bs);
			ops.flush();
			//定义字8节数组，读取头部信息，确定读取该请求的大小
			//最大传输字节数为99999999/1024/1024 = 95.367MB
			InputStream ips = socket.getInputStream();
			byte[] headdata = new byte[Const.REQUEST_HEAD_SIZE];
			int len = ips.read(headdata);
			//如果读取不足8字节
			if(len < Const.REQUEST_HEAD_SIZE) {
				ServiceResponse resp = new ServiceResponse();
				resp.setCode(ServiceResponse.ERROR_RESPONSE_HEAD);
				return resp;
			}
			//真实请求的字节长度
			int bodylen = 0;
			try {
				bodylen = Integer.valueOf(new String(headdata, "UTF-8"));
			} catch (NumberFormatException e) {
				//头部无法识别为数字
				ServiceResponse resp = new ServiceResponse();
				resp.setCode(ServiceResponse.ERROR_RESPONSE_HEAD);
				return resp;
			}
			//一次定义
			byte[] buf = new byte[bodylen];
			len = ips.read(buf);
			//如果读取不足真实请求的字节长度
			if(len < bodylen) {
				ServiceResponse resp = new ServiceResponse();
				resp.setCode(ServiceResponse.ERROR_RESPONSE_BODY);
				return resp;
			}
			ServiceResponse resp = SerializeUtil.deserialize(buf, ServiceResponse.class);
			return resp;
		} catch (IOException e) {
			logger.error("Error write response to client:" + e);
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * 检查参数对象是否是序列化的对象，
	 * 如果参数全部是序列化的对象则执行下一步，否则抛出没有序列化的异常
	 * @param args Object[]
	 * @return boolean
	 * true ： 参数合法<br>
	 * false : 参数不合法
	 */
	public static boolean isArgsSerializabled(Object[] args){
		boolean isSerializable = false;
		for(Object obj : args){
			int deep = MethodUtil.isSuperClass(Serializable.class, obj.getClass());
			if(deep == 0)
				isSerializable = false;
			else
				isSerializable = true;
			if(!isSerializable)
				return false;
		}
		return true;
	}
}

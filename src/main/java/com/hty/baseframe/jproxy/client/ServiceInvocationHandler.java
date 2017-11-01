package com.hty.baseframe.jproxy.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.Socket;

import com.hty.baseframe.jproxy.bean.MethodEntity;
import com.hty.baseframe.jproxy.bean.RemoteService;
import com.hty.baseframe.jproxy.bean.ServiceResponse;
import com.hty.baseframe.jproxy.exception.ServiceInvokationException;
/**
 * 对调用的接口方法进行切面控制。
 * @author Hetianyi
 * @version 1.0
 */
public class ServiceInvocationHandler implements InvocationHandler {
	
	private RemoteService rs;
	
	public ServiceInvocationHandler(RemoteService rs) {
		this.rs = rs;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		//包装序列化对象
		MethodEntity me  = new MethodEntity(); 
		me.setMethodName(method.getName());
		me.setObejctClass(rs.getClazz());
		me.setArgs(args);
		me.setArgsTypes(method.getParameterTypes());
		Socket socket = null;
		ClientSocketManager socketManager = ClientSocketManager.getInstance();
		try {
			//socket = SocketClientManager.getInstance().getPoolSocket(rs);
			socket = socketManager.getServiceSocket(rs);
			//System.out.println(Thread.currentThread().getName() + "释放"+ rs.getClazz() +"锁");
			//System.out.println(Thread.currentThread().getName() + " 得到了socket");
			ServiceResponse resp = null;
			resp = ServiceClient.sendRequest(rs, socket, me);
			//处理结果
			if(resp.getCode() != ServiceResponse.SUCCESS) {
				//远程处理发生内部错误
				if(resp.getCode() == ServiceResponse.INNER_ERROR) {
					socketManager.returnSocket(rs, socket);
					throw (Exception) resp.getResult();
				} else if(resp.getCode() == ServiceResponse.PERMISSION_DENIE_IP) {
					//连接失败：地址不被允许
					socketManager.returnSocket(rs, socket);
					throw new ServiceInvokationException("Invoke target method failed["+ resp.getCode() +"]:Your IP is not permit to call this service.");
				} else if(resp.getCode() == ServiceResponse.PERMISSION_DENIE_TOKEN) {
					//连接失败：token验证失败
					socketManager.returnSocket(rs, socket);
					throw new ServiceInvokationException("Invoke target method failed["+ resp.getCode() +"]:Invalid token.");
				} else if(resp.getCode() == ServiceResponse.ERROR_RESPONSE_HEAD) {
					//相应头部异常
					socketManager.returnBrokenSocket(rs, socket);
					throw new ServiceInvokationException("Invoke target method failed["+ resp.getCode() +"]:Invalid response data header.");
				} else if(resp.getCode() == ServiceResponse.ERROR_RESPONSE_BODY) {
					//相应体异常
					socketManager.returnBrokenSocket(rs, socket);
					throw new ServiceInvokationException("Invoke target method failed["+ resp.getCode() +"]:Invalid response data body.");
				}
				socketManager.returnBrokenSocket(rs, socket);
				throw new ServiceInvokationException("Invoke target method failed["+ resp.getCode() +"]");
			} else {
				//处理正常
				socketManager.returnSocket(rs, socket);
				return resp.getResult();
			}
		} catch (Exception e) {
			//System.out.println("Error class name = " + e.getClass().getName());
			if(null != socket) {
				if(e.getClass().getName().startsWith("java.net.")) {
					//Socket异常，要废弃当前socket
					socketManager.returnBrokenSocket(rs, socket);
				} else {
					//此异常为其他异常，如验证失败等，但是请求交互正常，socket状态正常。
					socketManager.returnSocket(rs, socket);
				}
			}
			throw e;
		}
	}

}

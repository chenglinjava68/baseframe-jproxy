package com.hty.baseframe.jproxy.client;

import com.hty.baseframe.jproxy.bean.MethodEntity;
import com.hty.baseframe.jproxy.bean.RemoteService;
import com.hty.baseframe.jproxy.bean.ServiceRequest;
import com.hty.baseframe.jproxy.bean.ServiceResponse;
import com.hty.baseframe.jproxy.exception.ServiceInvokationException;
import com.hty.baseframe.jproxy.tunel.client.ClientTunnel;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

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
		if(!isArgsSerializabled(args)) {
			throw new NotSerializableException("arg(s) of method " + me.getClass().getName() + "."+ me.getMethodName() +"() is not Serializable.");
		}
		me.setMethodName(method.getName());
		me.setObejctClass(rs.getClazz());
		me.setArgs(args);
		me.setArgsTypes(method.getParameterTypes());
		ClientSocketManager socketManager = ClientSocketManager.getInstance();
		String tunnelKey;
		try {
			//socket = SocketClientManager.getInstance().getPoolSocket(rs);

			//System.out.println(Thread.currentThread().getName() + "释放"+ rs.getClazz() +"锁");
			//System.out.println(Thread.currentThread().getName() + " 得到了socket");
			ServiceResponse resp = null;
			ServiceRequest req = new ServiceRequest();
			req.setMethodEntity(me);
			req.setClazz(rs.getClazz());
            //先获取旧的tunnelKey，如果存在就使用
            tunnelKey = socketManager.getTunnelKey(rs);
            if(null == tunnelKey || null == ClientTunnel.getSession(tunnelKey)) {
				ClientSocketManager.removeTunnel(tunnelKey);
                tunnelKey = socketManager.getTunnelKey(rs);
            }
            resp = ClientTunnel.write(req, tunnelKey);
			//处理结果
			if(resp.getCode() != ServiceResponse.SUCCESS) {
				//远程处理发生内部错误
				if(resp.getCode() == ServiceResponse.INNER_ERROR) {
					throw (Exception) resp.getResult();
				} else if(resp.getCode() == ServiceResponse.PERMISSION_DENIE_IP) {
					//连接失败：地址不被允许
					throw new ServiceInvokationException("Invoke target method failed["+ resp.getCode() +"]:Your IP is not permit to call this service.");
				} else if(resp.getCode() == ServiceResponse.PERMISSION_DENIE_TOKEN) {
					//连接失败：token验证失败
					throw new ServiceInvokationException("Invoke target method failed["+ resp.getCode() +"]:Invalid token.");
				} else if(resp.getCode() == ServiceResponse.ERROR_RESPONSE_HEAD) {
					//相应头部异常
					throw new ServiceInvokationException("Invoke target method failed["+ resp.getCode() +"]:Invalid response data header.");
				} else if(resp.getCode() == ServiceResponse.ERROR_RESPONSE_BODY) {
					//相应体异常
					throw new ServiceInvokationException("Invoke target method failed["+ resp.getCode() +"]:Invalid response data body.");
				}
				throw new ServiceInvokationException("Invoke target method failed["+ resp.getCode() +"]");
			} else {
				//处理正常
				return resp.getResult();
			}
		} catch (Exception e) {
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
		for(Object obj : args){
			if(!(obj instanceof Serializable)) {
				return false;
			}
		}
		return true;
	}
}

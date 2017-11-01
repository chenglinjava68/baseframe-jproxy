package com.hty.baseframe.jproxy.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Set;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hty.baseframe.common.util.StringUtil;
import com.hty.baseframe.jproxy.bean.LocalService;
import com.hty.baseframe.jproxy.bean.MethodEntity;
import com.hty.baseframe.jproxy.bean.ServiceRequest;
import com.hty.baseframe.jproxy.bean.ServiceResponse;
import com.hty.baseframe.jproxy.common.BeanProvider;
import com.hty.baseframe.jproxy.common.Const;
import com.hty.baseframe.jproxy.common.ServiceFactory;
import com.hty.baseframe.jproxy.util.SerializeUtil;
/**
 * 为服务提供者端的连入每个socket创建一个新线程，
 * 该线程处理该socket的素有请求，当发生socket异常，线程自动退出。
 * @author Tisnyi
 */
public class ServerSocketManager implements Runnable {
	
	private final Log logger = LogFactory.getLog(ServerSocketManager.class);
	
	private Socket client;
	
	private ServiceServer server;
	
	public static ThreadLocal<Socket> currentSocket = new ThreadLocal<Socket>();
	
	private int times = 0;
	
	private SocketListener listener;
	
	public ServerSocketManager(Socket client, ServiceServer server, SocketListener listener) {
		this.client = client;
		this.server = server;
		this.listener = listener;
	}
	/**
	 * 暴露此方法，让业务程序可获知此请求来自哪个地址
	 * @return
	 */
	public static Socket getCurrentSocket() {
		return currentSocket.get();
	}
	
	public void run() {
		currentSocket.set(client);
		if(null != listener) {
			listener.connect();
		}
		try {
			logger.info("got new connection from remote host @" + ((InetSocketAddress) client.getRemoteSocketAddress()).getAddress().getHostAddress() + ":" + client.getPort());
			InputStream ips = client.getInputStream();
			OutputStream ops = client.getOutputStream();
			while(true) {
				times++;
				//测试
				/*if(times % 100 == 0) {
					//System.out.println("server强制关闭client!!!!!!");
					break;
				}*/
				//logger.info("waiting to read data...");
				//定义字8节数组，读取头部信息，确定读取该请求的大小
				//最大传输字节数为99999999/1024/1024 = 95.367MB
				byte[] headdata = new byte[Const.REQUEST_HEAD_SIZE];
				int len = ips.read(headdata);
				//如果读取不足8字节
				if(len < Const.REQUEST_HEAD_SIZE) {
					ServiceResponse resp = new ServiceResponse();
					resp.setCode(ServiceResponse.ERROR_REQUEST_HEAD);
					writeResponse(resp, ops, true);
					client.close();
					break;
				}
				//真实请求的字节长度
				int bodylen = 0;
				try {
					bodylen = Integer.valueOf(new String(headdata, "UTF-8"));
				} catch (NumberFormatException e) {
					//头部无法识别为数字
					ServiceResponse resp = new ServiceResponse();
					resp.setCode(ServiceResponse.ERROR_REQUEST_HEAD);
					writeResponse(resp, ops, true);
					client.close();
					break;
				}
				//一次定义
				byte[] buf = new byte[bodylen];
				len = ips.read(buf);
				//如果读取不足真实请求的字节长度
				if(len < bodylen) {
					ServiceResponse resp = new ServiceResponse();
					resp.setCode(ServiceResponse.ERROR_REQUEST_BODY);
					writeResponse(resp, ops, true);
					client.close();
					break;
				}
				ServiceRequest request = SerializeUtil.deserialize(buf, ServiceRequest.class);
				
				//检查调用权限
				int status = accessCheck(request);
				if(status == 1) {
					ServiceResponse resp = invokeServiceMethod(request);
					writeResponse(resp, ops, false);
				} else {
					ServiceResponse resp = new ServiceResponse();
					resp.setCode(status);
					writeResponse(resp, ops, false);
				}
				continue;
			}
		} catch (IOException e) {
			logger.error("ServerSocketManager error:" + e + ", Socket will close.");
			e.printStackTrace();
		} finally {
			server.flagConnection(0);
			if(null != listener) {
				listener.disconnect();
			}
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 将响应写回到客户端
	 * @param resp
	 * @param ops
	 * @param shutdown
	 */
	private void writeResponse(ServiceResponse resp, OutputStream ops, boolean shutdown) {
		if(null != resp) {
			byte[] bs = SerializeUtil.serialize(resp);
			String head = SerializeUtil.getHeadString(bs.length, Const.REQUEST_HEAD_SIZE);
			try {
				ops.write(head.getBytes("UTF-8"));
				ops.write(bs);
				ops.flush();
			} catch (IOException e) {
				logger.error("Error write response to client:" + e + "\n");
				e.printStackTrace();
			}
			if(shutdown) {
				logger.error("Socket will close by server.");
				try {
					ops.close();
				} catch (IOException e) {}
			}
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
			String version = request.getVersion();
			MethodEntity me = request.getMethodEntity();
			LocalService localService = ServiceFactory.getLocalService(clazz, version);
			BeanProvider provider = ServiceFactory.getBeanProvider();
			Object bizService = provider.getBean(localService.getClazz(), version);
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
	private int accessCheck(ServiceRequest request) {
		//检查IP名单
		Class<?> interfaceClass = request.getClazz();
		String version = request.getVersion();
		LocalService localService = ServiceFactory.getLocalService(interfaceClass, version);
		Object token = request.getParameter("token");
		Set<String> clients = localService.getClients();
		//System.out.println("clients="+clients);
		if(null != clients && !clients.isEmpty()) {
			//校验是否服务的白名单
			String[] parts;
			InetSocketAddress addr = (InetSocketAddress) client.getRemoteSocketAddress();
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

package com.hty.baseframe.jproxy.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.hty.baseframe.jproxy.registry.loader.CandidateProvider;
import com.hty.baseframe.jproxy.registry.loader.ServiceConsumer;

/**
 * 网络接口工具类，获取本机拥有的IP地址和测试远程主机是否可到达。
 * @author Tisnyi
 */
public class NetWorkInterfaceUtil {
	
	private static final String ipv4_regex = "[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+";
	/**
	 * 获取本机拥有的网络IP地址列表
	 * @return
	 */
	public static Set<String> getLocalHostAddress() {
		Set<String> ret = new HashSet<String>();
		try {
			Enumeration<NetworkInterface> ns = NetworkInterface.getNetworkInterfaces();
			if(null != ns)
			while(ns.hasMoreElements()) {
				NetworkInterface ni = ns.nextElement();
				Enumeration<InetAddress> adds = ni.getInetAddresses();
				if(null != adds)
				while(adds.hasMoreElements()) {
					String hostaddr = adds.nextElement().getHostAddress();
					if(matchIPV4Address(hostaddr)) {
						ret.add(hostaddr);
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return ret;
	}
	/**
	 * 检查地址是否符合IPV4(同时过滤特殊地址：127.0.0.1)
	 * @param hostAddress
	 * @return
	 */
	private static boolean matchIPV4Address(String hostAddress) {
		if(null == hostAddress) return false;
		if("127.0.0.1".equals(hostAddress) || "127.0.0.0".equals(hostAddress))
		return false;
		return hostAddress.matches(ipv4_regex);
	}
	
	/**
	 * 从主机IP地址集合选取最合适连接的地址
	 * @return
	 */
	public static String pickHostCandidate(ServiceConsumer consumer, CandidateProvider provider, 
			Set<String> excludes) {
		//Set<String> localHostIPs = consumer.getAddresses();
		Set<String> remoteHostIPs = provider.getAddresses();
		//先选取同一网段的
		if(null != remoteHostIPs) {
			for(Iterator<String> it = remoteHostIPs.iterator(); it.hasNext();) {
				String host = it.next();
				if(excludes.contains(host))
					continue;
				return host;
			}
		}
		if(!excludes.contains(provider.getProviderLookBackAddress()))
			return provider.getProviderLookBackAddress();
		return null;
	}
	/**
	 * 判断是否同一网段的近似粗暴算法
	 * @param host
	 * @param lhost
	 * @return
	 */
	private static boolean isSameNetworkLike(String host, String lhost) {
		String[] phost = host.split("\\.");
		String[] plhost = lhost.split("\\.");
		int level = 0;
		for(int i = 0; i < phost.length; i++) {
			if(phost[i].equals(plhost[i])) {
				level++;
			} else {
				if(level >= 2) {
					return true;
				}
				return false;
			}
		}
		return true;
	}
	/**
	 * 检测远程主机是否可到达
	 * @param host
	 * @return
	 */
	public static boolean hostReachale(String host) {
		int  timeOut =  3000 ;
        boolean status;
		try {
			status = InetAddress.getByName(host).isReachable(timeOut);
		} catch (Exception e) {
			return false;
		}
        return status;
	}
}

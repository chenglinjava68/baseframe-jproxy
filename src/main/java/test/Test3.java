package test;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;

import com.hty.baseframe.jproxy.util.NetWorkInterfaceUtil;

public class Test3 {
	
	private static List<String> demo = new LinkedList<String>();
	
	/**
	 * @param args
	 * @throws SocketException 
	 */
	public static void main(String[] args) throws SocketException {
		/*Enumeration<NetworkInterface> ns = NetworkInterface.getNetworkInterfaces();
		if(null != ns)
		while(ns.hasMoreElements()) {
			System.out.println("网络:-----------------------");
			NetworkInterface ni = ns.nextElement();
			System.out.println(ni.getDisplayName());
			Enumeration<InetAddress> adds = ni.getInetAddresses();
			if(null != adds)
			while(adds.hasMoreElements()) {
				System.out.println(adds.nextElement().getHostAddress());
			}
		}*/
		
		System.out.println(NetWorkInterfaceUtil.getLocalHostAddress());
		
		
		new Thread(new TestThread1()).start();
		new Thread(new TestThread2()).start();
		
		InetAddress ip = null;
        NetworkInterface ni = null;
        try {
            ip = InetAddress.getLocalHost();
            ni = NetworkInterface.getByInetAddress(ip);// 搜索绑定了指定IP地址的网络接口
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<InterfaceAddress> list = ni.getInterfaceAddresses();// 获取此网络接口的全部或部分
                                                                    // InterfaceAddresses
                                                                    // 所组成的列表
        if (list.size() > 0) {
            int mask = list.get(0).getNetworkPrefixLength(); // 子网掩码的二进制1的个数
            StringBuilder maskStr = new StringBuilder();
            int[] maskIp = new int[4];
            for (int i = 0; i < maskIp.length; i++) {
                maskIp[i] = (mask >= 8) ? 255 : (mask > 0 ? (mask & 0xff) : 0);
                mask -= 8;
                maskStr.append(maskIp[i]);
                if (i < maskIp.length - 1) {
                    maskStr.append(".");
                }
            }
            System.out.println("SubnetMask:" + maskStr);
        }
        
	}
	
	private static class TestThread1 implements Runnable {
		@Override
		public void run() {
			synchronized (demo) {
				demo.add("1111");
			}
		}
	}
	
	private static class TestThread2 implements Runnable {
		@Override
		public void run() {
			synchronized (demo) {
				demo.add("2222");
			}
		}
	}
	
}

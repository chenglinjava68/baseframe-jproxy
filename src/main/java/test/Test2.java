package test;


import java.util.Timer;
import java.util.TimerTask;

import test.beans.TestUser;
import test.service.TestService;

import com.hty.baseframe.jproxy.JProxy;
import com.hty.baseframe.jproxy.client.ClientSocketManager;
import com.hty.baseframe.jproxy.common.ServiceFactory;

public class Test2 {
	
	static long start = System.currentTimeMillis();
	public static void main(String[] args) throws Exception {
		JProxy jp = new JProxy();
		jp.start("jproxy.xml");
		new Timer().scheduleAtFixedRate(new PrintTask(), 0L, 5000L);
		for(int i = 0; i < 7; i++) {
			Thread t1 = new Thread(new Task());
			t1.setName("Mother-Fucker-" + i);
			t1.start();
		}
	}
	private static int count = 0;
	
	private static class PrintTask extends TimerTask {
		@Override
		public void run() {
			long end = System.currentTimeMillis();
			System.out.println(count + "--" + (end-start)*1.0/1000 + "[ms/个]" + "--TestService的socket创建次数：" + ClientSocketManager.count);
			if(count >= 7*500) {
				System.exit(0);
			}
		}
	}
	
	private synchronized static final void update() {
		count++;
	}
	
	private static class Task implements Runnable {

		@Override
		public void run() {
			TestService ts = ServiceFactory.getProxyInstance(TestService.class, "1.0");
			TestUser tu = new TestUser(100, "zhangsan");
			for(int i = 0; i < 500 ;i++) {
				String ret;
				try {
					long a = System.currentTimeMillis();
					ret = ts.save(tu);
					//System.out.println(System.currentTimeMillis() - a);
					System.out.println(ret);//java.lang.Object@67d1a6b3
					update();
				} catch (Exception e) {
					System.out.println("出现错误，不管了，继续！");
					e.printStackTrace();
				}
			}
			System.out.println("=================================================finish!!!!!!");
		}
		
	}
	
	
}

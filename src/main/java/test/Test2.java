package test;


import com.hty.baseframe.jproxy.JProxy;
import com.hty.baseframe.jproxy.common.ServiceFactory;
import test.beans.TestUser;
import test.service.TestService;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Test2 {
	
	static long start = System.currentTimeMillis();
	public static void main(String[] args) throws Exception {
		JProxy jp = new JProxy();
		jp.start("jproxy.xml");
		new Timer().scheduleAtFixedRate(new PrintTask(), 0L, 5000L);
		for(int i = 0; i < 50; i++) {
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
			System.out.println(count + "--" + (500000/((end-start)*1.0/1000)) + "[次/s]" + "--TestService的socket创建次数：");

		}
	}
	
	private synchronized static final void update() {
		count++;
	}
	
	private static class Task implements Runnable {

		@Override
		public void run() {
			Map<String, String> map = new HashMap<String, String>();
			map.put("dbNum", "12");
			TestService ts = ServiceFactory.getProxyInstance(TestService.class);

			TestUser tu = new TestUser(100, "zhangsan");
			for(int i = 0; i < 10000 ;i++) {
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

package test;

import com.hty.baseframe.jproxy.JProxy;

public class Test1 {
	public static void main(String[] args) throws Exception {
		JProxy jp = new JProxy();
		jp.start("jproxy-server.xml");
	}
}

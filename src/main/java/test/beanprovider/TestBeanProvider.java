package test.beanprovider;

import test.service.impl.TestServiceImpl;

import com.hty.baseframe.jproxy.common.BeanProvider;

public class TestBeanProvider implements BeanProvider {
	
	@Override
	public Object getBean(Class<?> type) {
		return new TestServiceImpl();
	}

}

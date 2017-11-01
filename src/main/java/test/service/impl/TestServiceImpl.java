package test.service.impl;

import test.beans.TestUser;
import test.service.TestService;

public class TestServiceImpl implements TestService {

	@Override
	public String save(TestUser user) {
		System.out.println(user);
//		if(true)
//		throw new RuntimeException("Fuck you!");
		return "Fuck you!";
	}

}

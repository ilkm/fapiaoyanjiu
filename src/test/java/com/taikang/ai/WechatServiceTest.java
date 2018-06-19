package com.taikang.ai;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.taikang.jkx.RootApplication;
import com.taikang.jkx.util.AccessTokenManager;

@SpringBootTest(classes=RootApplication.class)
@RunWith(SpringRunner.class)
public class WechatServiceTest {
	
	@Autowired
	private AccessTokenManager accessTokenManager;

	@Test
	public void fun1(){
		try {
			String accessToken = accessTokenManager.getAccessToken();
			System.out.println(accessToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

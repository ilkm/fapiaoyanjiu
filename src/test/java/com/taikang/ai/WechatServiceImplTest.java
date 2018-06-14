package com.taikang.ai;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.taikang.jkx.RootApplication;
import com.taikang.jkx.inteface.GSJService;
import com.taikang.jkx.inteface.WechatService;
import com.taikang.jkx.util.AccessTokenManager;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=RootApplication.class)
@EnableAutoConfiguration
public class WechatServiceImplTest {
	@Autowired
	private GSJService gsjService;
	@Autowired
	private WechatService wechatService;
	@Autowired
	private AccessTokenManager accessTokenManager;

	@Test
	public void fun1(){
		String accessToken;
		try {
			accessToken = accessTokenManager.getAccessToken();
			System.out.println(accessToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

package com.taikang.jkx.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

/**
 * accessToken管理类
 */
@Component
public class AccessTokenManager {

	@Value("${wechat.accessTokenExpireTime}")
	private long accessTokenExpireTime;
	@Value("${wechat.accessTokenUrl}")
	private String accessTokenGetUrl;
	@Autowired
	private HttpClientCreator httpClientCreator;

	/**
	 * 从accessToken.properties中读取accessToken，如果accessToken已经过期，那么重新获取。
	 * @return
	 * @throws IOException
	 */
	public String getAccessToken() throws IOException {

		File accessTokenFile = new File("accessToken.properties");
		Properties prop = new Properties();
		if (accessTokenFile.exists()) {
			InputStream resourceAsStream = new FileInputStream(accessTokenFile);
			prop.load(resourceAsStream);
			String accessToken = prop.getProperty("accessToken");
			String createTime = prop.getProperty("createTime");
			// 如果上次请求的accessToken的存活日期已经超过了两个小时，就重新获取
			if (System.currentTimeMillis() - Long.valueOf(createTime) < accessTokenExpireTime) {
				return accessToken;
			}
		} 
		

		//从微信公众平台获取accessToken
		CloseableHttpClient httpClient = httpClientCreator.getHttpClient();
		HttpGet get = new HttpGet(accessTokenGetUrl);
		
		CloseableHttpResponse execute = httpClient.execute(get);
		HttpEntity entity = execute.getEntity();
		InputStream content = entity.getContent();
		//解析从公众平台得到的响应信息
		Reader reader = new InputStreamReader(content);
		char[] chartemp = new char[1024];
		int read = reader.read(chartemp );
		String str = new String(chartemp, 0, read);
		
		JSONObject json = JSONObject.parseObject(str);
		if(json!=null){
			String accessToken = json.getString("access_token");
			prop.put("accessToken", accessToken);
			prop.put("createTime", ""+System.currentTimeMillis());
		}
		
		prop.store(new FileWriter(accessTokenFile), "");
		
		return prop.getProperty("accessToken");
	}

}

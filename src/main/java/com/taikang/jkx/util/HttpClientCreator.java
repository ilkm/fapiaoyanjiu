package com.taikang.jkx.util;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HttpClientCreator {
	
	@Value("${proxy.enable}")
	private boolean proxyEnable;
	@Value("${proxy.host}")
	private String proxyHost;
	@Value("${proxy.port}")
	private int proxyPort = 3088;
	@Value("${proxy.username}")
	private String username;
	@Value("${proxy.password}")
	private String password;
	private HttpClientBuilder create;
	/**
	 * 创建httpClient客户端
	 * 
	 * @return
	 */
	public CloseableHttpClient getHttpClient() {
		// 创建客户端
		if(create!=null){
			return create.build();
		}
		create = HttpClientBuilder.create();
		if(proxyEnable){
			// 设置代理服务器信息
			HttpHost proxy = new HttpHost(proxyHost, proxyPort);
			create.setProxy(proxy);
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			Credentials credentials = new UsernamePasswordCredentials(username, password);
			credentialsProvider.setCredentials(AuthScope.ANY, credentials);
			create.setDefaultCredentialsProvider(credentialsProvider);
		}
		// 设置域名验证逻辑
		create.setSSLHostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		});
		// 获取客户端
		return create.build();
	}
}

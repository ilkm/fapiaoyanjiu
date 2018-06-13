package com.taikang.jkx.inteface.impl;

import java.io.IOException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.taikang.jkx.inteface.GSJService;

/**
 * 实现操作国税局网站的相关操作
 * 
 * @author zhangqh27
 *
 */
@Service
public class GSJServiceImpl implements GSJService {

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
	@Value("${gsj.gsjUrl}")
	private String gsjUrl;
	@Value("${gsj.captchaUrl}")
	private String captchaUrl;

	@Override
	public String getSessionIDFromGsj() throws ClientProtocolException, IOException {

		// 先从数据库中查看当前微信用户是否已存在sessionID。
		String JSESSIONID = "";
		// 获取客户端
		CloseableHttpClient client = getHttpClient();

		// 创建get请求地址
		HttpGet get = new HttpGet(gsjUrl);
		HttpResponse execute = client.execute(get);
		// 获取响应状态码
		StatusLine statusLine = execute.getStatusLine();
		System.out.println(statusLine);
		// 获取session识别码
		// Set-Cookie:JSESSIONID=o9LyqpSYohchmBTjr9_60L7LVScagBuhVYRkT1XkDr-DsysRyK-w!-1834616633;
		// path=/; HttpOnly
		Header cookieHeader = execute.getFirstHeader("Set-Cookie");
		if (cookieHeader != null) {
			String[] split = cookieHeader.getValue().split(";");
			if (split != null && split.length > 0) {
				String jsessionIdStr = split[0];
				String[] jsessionIdPeers = jsessionIdStr.split("=");
				if (jsessionIdPeers != null && jsessionIdPeers.length > 1) {
					JSESSIONID = jsessionIdPeers[1];
				}
			}
		}

		client.close();
		return JSESSIONID;
	}

	/**
	 * 创建httpClient客户端
	 * 
	 * @return
	 */
	private CloseableHttpClient getHttpClient() {
		// 创建客户端
		HttpClientBuilder create = HttpClientBuilder.create();
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
		CloseableHttpClient client = create.build();
		return client;
	}

	@Override
	public void getCaptchaBySessionID(String sessionID) throws ClientProtocolException, IOException {
		CloseableHttpClient httpClient = getHttpClient();
		// 创建get请求地址
		HttpGet get = new HttpGet(captchaUrl);
		get.setHeader("Cookie", "JSESSIONID="+sessionID);
		HttpResponse execute = httpClient.execute(get);
		StatusLine statusLine = execute.getStatusLine();
		System.out.println(statusLine);
		HttpEntity entity = execute.getEntity();
		

	}

}

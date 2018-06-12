package com.taikang.jkx.inteface.impl;

import java.io.IOException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.http.Header;
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
import org.springframework.stereotype.Service;

import com.taikang.jkx.inteface.GSJService;

/**
 * 实现操作国税局网站的相关操作
 * @author zhangqh27
 *
 */
@Service
public class GSJServiceImpl implements GSJService {

	@Override
	public String getSessionIDFromGsj(String userId) throws ClientProtocolException, IOException {
		
		//先从数据库中查看当前微信用户是否已存在sessionID。
		String JSESSIONID = "";
		//创建客户端
		HttpClientBuilder create = HttpClientBuilder.create();
		//设置代理服务器信息
		HttpHost proxy = new HttpHost("10.11.2.40", 3088);
		create.setProxy(proxy);
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		Credentials credentials = new UsernamePasswordCredentials("zhangqh27", "El250816");
		credentialsProvider.setCredentials(AuthScope.ANY, credentials );
		create.setDefaultCredentialsProvider(credentialsProvider );
		//设置域名验证逻辑
		create.setSSLHostnameVerifier(new HostnameVerifier() {
			
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		});
		//获取客户端
		CloseableHttpClient client = create.build();
		
		//创建get请求地址
		HttpGet get = new HttpGet("https://59.173.248.30:7013/include1/cx_sgfplxcx.jsp");
		HttpResponse execute = client.execute(get);
		//获取响应状态码
		StatusLine statusLine = execute.getStatusLine();
		System.out.println(statusLine);
		//获取session识别码
		Header[] allHeaders = execute.getAllHeaders();
		if(allHeaders!=null){
			int length = allHeaders.length;
			for(int i=0;i<length;i++){
				Header header = allHeaders[i];
				String name = header.getName();
				String value = header.getValue();
				System.out.println(name+":"+value);
				//Set-Cookie:JSESSIONID=o9LyqpSYohchmBTjr9_60L7LVScagBuhVYRkT1XkDr-DsysRyK-w!-1834616633; path=/; HttpOnly
				if("Set-Cookie".equals(name)){
					String[] split = value.split(";");
					if(split!=null&&split.length>0){
						String jsessionIdStr = split[0];
						String[] jsessionIdPeers = jsessionIdStr.split("=");
						if(jsessionIdPeers!=null&&jsessionIdPeers.length>1){
							JSESSIONID = jsessionIdPeers[1];
						}
					}
					break;
				}
			}
		}
		
		client.close();
		return JSESSIONID;
	}

}

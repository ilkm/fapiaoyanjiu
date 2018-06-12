package com.taikang.ai;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

public class HttpClientTest {

	/**
	 * 测试HttpClient
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	@Test
	public void fun3() throws ClientProtocolException, IOException {
		//创建客户端
		HttpClient client = HttpClientBuilder.create().build();
		//创建get请求地址
		HttpGet get = new HttpGet("https://zhqh.hb-n-tax.gov.cn:7013/include1/cx_sgfplxcx.jsp");
		HttpResponse execute = client.execute(get);
		
		StatusLine statusLine = execute.getStatusLine();
		System.out.println(statusLine);
		Header[] allHeaders = execute.getAllHeaders();
		if(allHeaders!=null){
			int length = allHeaders.length;
			for(int i=0;i<length;i++){
				Header header = allHeaders[i];
				String name = header.getName();
				String value = header.getValue();
				System.out.println(name+":"+value);
			}
		}
		
	}
}

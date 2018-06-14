package com.taikang.jkx.inteface.impl;

import java.io.IOException;
import java.io.InputStream;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.taikang.jkx.bo.CaptchaBO;
import com.taikang.jkx.bo.GsjSession;
import com.taikang.jkx.inteface.GSJService;
import com.taikang.jkx.util.GsjSessionUtil;
import com.taikang.jkx.util.HttpClientCreator;

/**
 * 实现操作国税局网站的相关操作
 * 
 * @author zhangqh27
 *
 */
@Service
public class GSJServiceImpl implements GSJService {

	@Autowired
	private HttpClientCreator httpClinetCreator;
	
	@Value("${gsj.gsjUrl}")
	private String gsjUrl;
	@Value("${gsj.captchaUrl}")
	private String captchaUrl;
	@Value("${gsj.sessionIdExpireTime}")
	private long gsjSessionExpireTime;

	@Override
	public String getSessionIDFromGsj(String userId) throws ClientProtocolException, IOException {

		//先判断本地系统中存储的sessionID是否已过期，如果过期了就移除。
		GsjSessionUtil.expireGsjSesionByUserId(userId, gsjSessionExpireTime);
		// 先从数据库中查看当前微信用户是否已存在sessionID。,如果没有,请求网站获取一个.
		GsjSession sessionByWechatUserId = GsjSessionUtil
				.getSessionByWechatUserId(userId);
		if(sessionByWechatUserId!=null){
			return sessionByWechatUserId.getGsjSessionId();
		}
		String JSESSIONID = "";
		GsjSessionUtil.getSessionByWechatUserId(userId);
		// 获取客户端
		CloseableHttpClient client = httpClinetCreator.getHttpClient();

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
		
		GsjSession jSession = new GsjSession();
		jSession.setCreateTime(System.currentTimeMillis());
		jSession.setGsjSessionId(JSESSIONID);
		GsjSessionUtil.setGsjSession(userId, jSession);
		client.close();
		
		return JSESSIONID;
	}

	

	@Override
	public CaptchaBO getCaptchaBySessionID(String sessionID) throws ClientProtocolException, IOException {
		CloseableHttpClient httpClient = httpClinetCreator.getHttpClient();
		// 创建get请求地址
		HttpGet get = new HttpGet(captchaUrl);
		get.setHeader("Cookie", "JSESSIONID="+sessionID);
		HttpResponse execute = httpClient.execute(get);
		StatusLine statusLine = execute.getStatusLine();
		System.out.println(statusLine);
		HttpEntity entity = execute.getEntity();
		InputStream content = entity.getContent();
		String contentType = entity.getContentType().getValue();
		long contentLength = entity.getContentLength();
		
		CaptchaBO captcha = new CaptchaBO();
		captcha.setInputStream(content);
		captcha.setContentLength(contentLength);
		captcha.setContentType(contentType);
		
		return captcha;
	}

}

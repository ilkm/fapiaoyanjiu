package com.taikang.jkx.inteface;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;

import com.taikang.jkx.bo.CaptchaBO;
import com.taikang.jkx.bo.GsjSession;

/**
 * 国税局网站的相关操作
 * @author zhangqh27
 *
 */
public interface GSJService {
	
	public GsjSession getSessionIDFromGsj(String userId) throws ClientProtocolException, IOException;
	
	public CaptchaBO getCaptchaBySessionID(String sessionID) throws ClientProtocolException, IOException;
	
	public String getMd5v(String fpdm,String fphm,String sessionId) throws ClientProtocolException, IOException;

	public String check(GsjSession sessionByWechatUserId, String content) throws UnsupportedEncodingException, ClientProtocolException, IOException;

}

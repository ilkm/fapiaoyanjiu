package com.taikang.jkx.inteface;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import com.taikang.jkx.bo.CaptchaBO;

/**
 * 国税局网站的相关操作
 * @author zhangqh27
 *
 */
public interface GSJService {
	
	public String getSessionIDFromGsj(String userId) throws ClientProtocolException, IOException;
	
	public CaptchaBO getCaptchaBySessionID(String sessionID) throws ClientProtocolException, IOException;

}

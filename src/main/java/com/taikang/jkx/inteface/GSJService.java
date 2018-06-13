package com.taikang.jkx.inteface;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

/**
 * 国税局网站的相关操作
 * @author zhangqh27
 *
 */
public interface GSJService {
	
	public String getSessionIDFromGsj() throws ClientProtocolException, IOException;
	
	public void getCaptchaBySessionID(String sessionID) throws ClientProtocolException, IOException;

}

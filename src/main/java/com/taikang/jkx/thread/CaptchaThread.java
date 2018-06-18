package com.taikang.jkx.thread;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.springframework.context.ApplicationContext;

import com.taikang.jkx.bo.CaptchaBO;
import com.taikang.jkx.bo.CommonUtil;
import com.taikang.jkx.bo.GsjSession;
import com.taikang.jkx.bo.WeChatCommunicationBO;
import com.taikang.jkx.inteface.GSJService;
import com.taikang.jkx.inteface.WechatService;
import com.taikang.jkx.util.ApplicationContextHolder;
import com.taikang.jkx.util.GsjSessionUtil;

/**
 * 该类用于想国税局网站请求验证码,并返回给用户
 * @author Administrator
 *
 */
public class CaptchaThread implements Runnable {
	
	private WeChatCommunicationBO realUserMessage;
	
	

	public CaptchaThread(WeChatCommunicationBO realUserMessage) {
		super();
		this.realUserMessage = realUserMessage;
	}



	@Override
	public void run() {
		ApplicationContext applicationContext = ApplicationContextHolder.getApplicationContext();
		GSJService gsjService = applicationContext.getBean(GSJService.class);
		WechatService wechatService = applicationContext.getBean(WechatService.class);
		
		GsjSession commonSession = GsjSessionUtil.getSessionByWechatUserId(GsjSessionUtil.COMMON_USER_ID);
		//拿着国税局网站的sessionID去请求验证码图片
		CaptchaBO captchaBySessionID;
		try {
			//从国税局网站获取验证码信息
			captchaBySessionID = gsjService.getCaptchaBySessionID(commonSession.getGsjSessionId());
			//将从国税局拿到的验证码作为临时图片素材上传到微信公众平台
			String mediaId = wechatService.uploadTempMedia(captchaBySessionID);
			wechatService.replyToUser(realUserMessage.getFromUserName(), CommonUtil.MessageTypeText, mediaId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	

}

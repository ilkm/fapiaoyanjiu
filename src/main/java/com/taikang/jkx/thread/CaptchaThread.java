package com.taikang.jkx.thread;

import org.springframework.context.ApplicationContext;

import com.taikang.jkx.bo.CaptchaBO;
import com.taikang.jkx.bo.CommonUtil;
import com.taikang.jkx.inteface.GSJService;
import com.taikang.jkx.inteface.WechatService;
import com.taikang.jkx.util.ApplicationContextHolder;

/**
 * 该类用于想国税局网站请求验证码,并返回给用户
 * @author Administrator
 *
 */
public class CaptchaThread implements Runnable {

	@Override
	public void run() {
		ApplicationContext applicationContext = ApplicationContextHolder.getApplicationContext();
		GSJService gsjService = applicationContext.getBean(GSJService.class);
		WechatService wechatService = applicationContext.getBean(WechatService.class);
		//拿着国税局网站的sessionID去请求验证码图片
		CaptchaBO captchaBySessionID = gsjService.getCaptchaBySessionID(sessionIDFromGsj.getGsjSessionId());
		//将从国税局拿到的验证码作为临时图片素材上传到微信公众平台
		String mediaId = wechatService.uploadTempMedia(captchaBySessionID);
		result = generateResponse(realUserMessage, CommonUtil.MessageTypeImage, mediaId);
		
	}
	
	

}

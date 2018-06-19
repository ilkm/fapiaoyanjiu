package com.taikang.jkx.thread;

import org.springframework.context.ApplicationContext;

import com.taikang.jkx.bo.CommonUtil;
import com.taikang.jkx.bo.GsjSession;
import com.taikang.jkx.bo.WeChatCommunicationBO;
import com.taikang.jkx.inteface.GSJService;
import com.taikang.jkx.inteface.WechatService;
import com.taikang.jkx.util.ApplicationContextHolder;
import com.taikang.jkx.util.GsjSessionUtil;

/**
 * 异步查验验旧信息,并将信息返回给用户
 * @author Administrator
 *
 */
public class CheckThread implements Runnable {
	
	private WeChatCommunicationBO realUserMessage;

	
	public CheckThread(WeChatCommunicationBO realUserMessage) {
		super();
		this.realUserMessage = realUserMessage;
	}
	
	@Override
	public void run() {
		ApplicationContext applicationContext = ApplicationContextHolder.getApplicationContext();
		GSJService gsjService = applicationContext.getBean(GSJService.class);
		WechatService wechatService = applicationContext.getBean(WechatService.class);
		GsjSession commonUserSession = GsjSessionUtil.getSessionByWechatUserId(GsjSessionUtil.COMMON_USER_ID);
		commonUserSession.setYanzhengma(realUserMessage.getContent());
		try {
			String yanjiuMessage = gsjService.check(GsjSessionUtil.getSessionByWechatUserId(realUserMessage.getFromUserName()),realUserMessage.getContent(),commonUserSession.getGsjSessionId());
			wechatService.replyToUser(realUserMessage.getFromUserName(), CommonUtil.MessageTypeText,yanjiuMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

package com.taikang.jkx.thread;

import java.util.List;
import java.util.Map;

import com.taikang.jkx.bo.GsjSession;
import com.taikang.jkx.bo.SampleBO;
import com.taikang.jkx.bo.WeChatCommunicationBO;
import com.taikang.jkx.inteface.AipOcrClientService;
import com.taikang.jkx.util.GsjSessionUtil;

public class OcrThread implements Runnable {
	
	private WeChatCommunicationBO messageFromXML;
	private AipOcrClientService aipOcrClient;

	public OcrThread(WeChatCommunicationBO wc,AipOcrClientService aipOcrClient){
		this.messageFromXML = wc;
		this.aipOcrClient = aipOcrClient;
	}
	
	public OcrThread() {
		super();
	}

	@Override
	public void run() {
		SampleBO basicGeneralUrl = aipOcrClient.basicGeneralUrl(messageFromXML.getPicUrl());
		List<Map<String, String>> words_result = basicGeneralUrl.getWords_result();
		if(words_result!=null&&words_result.size()>1){
			GsjSession jSession2 = GsjSessionUtil
					.getSessionByWechatUserId(messageFromXML.getFromUserName());
			jSession2.setFaPiaoDaiMa(words_result.get(0).get("words"));
			jSession2.setFaPiaoHaoMa(words_result.get(1).get("words"));
		}
	}

	public WeChatCommunicationBO getMessageFromXML() {
		return messageFromXML;
	}

	public void setMessageFromXML(WeChatCommunicationBO messageFromXML) {
		this.messageFromXML = messageFromXML;
	}

	public AipOcrClientService getAipOcrClient() {
		return aipOcrClient;
	}

	public void setAipOcrClient(AipOcrClientService aipOcrClient) {
		this.aipOcrClient = aipOcrClient;
	}
}

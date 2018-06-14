package com.taikang.jkx.thread;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.http.impl.client.CloseableHttpClient;

import com.taikang.jkx.bo.GsjSession;
import com.taikang.jkx.bo.SampleBO;
import com.taikang.jkx.bo.WeChatCommunicationBO;
import com.taikang.jkx.inteface.AipOcrClientService;
import com.taikang.jkx.inteface.GSJService;
import com.taikang.jkx.inteface.impl.GSJServiceImpl;
import com.taikang.jkx.util.GsjSessionUtil;

public class OcrThread implements Runnable {
	
	private WeChatCommunicationBO messageFromXML;
	private AipOcrClientService aipOcrClient;
	private CloseableHttpClient httpClient;

	public OcrThread(WeChatCommunicationBO wc,AipOcrClientService aipOcrClient, CloseableHttpClient httpClient){
		this.messageFromXML = wc;
		this.aipOcrClient = aipOcrClient;
		this.httpClient = httpClient;
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
			String fpdm = words_result.get(0).get("words");
			String fphm = words_result.get(1).get("words");
			jSession2.setFaPiaoDaiMa(fpdm);
			jSession2.setFaPiaoHaoMa(fphm);
			
			GSJService gsjService = new GSJServiceImpl();
			try {
				String md5v = gsjService.getMd5v(fpdm, fphm, GsjSessionUtil.getSessionByWechatUserId(messageFromXML.getFromUserName()).getGsjSessionId(),httpClient);
				jSession2.setMd5v(md5v);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
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

package com.taikang.jkx.controller;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.taikang.jkx.bo.CommonUtil;
import com.taikang.jkx.bo.GsjSession;
import com.taikang.jkx.bo.SampleBO;
import com.taikang.jkx.bo.WeChatCommunicationBO;
import com.taikang.jkx.inteface.AipOcrClientService;
import com.taikang.jkx.inteface.GSJService;
import com.taikang.jkx.util.GsjSessionUtil;

@RestController
public class WXController {

	@Autowired
	private AipOcrClientService aipOcrClient;
	@Autowired
	private GSJService gsjService;

	@PostMapping("/wx")
	public String hello(HttpServletRequest request, String signature, String timestamp, int nonce, String echostr)
			throws IOException, JDOMException, ParserConfigurationException, SAXException {

		String result = "";
		// 解析消息内容
		WeChatCommunicationBO messageFromXML = getMessageFromXML(request);

		// 根据消息内容调用逻辑
		// 如果发送的是文字信息直接返回接收到的文字信息
		if (CommonUtil.MessageTypeText.equals(messageFromXML.getMsgType())) {
			result = generateResponse(messageFromXML, CommonUtil.MessageTypeText, messageFromXML.getContent());
		}
		// 如果发送的是图片信息,调用文字识别接口进行文字识别
		if (CommonUtil.MessageTypeImage.equals(messageFromXML.getMsgType())) {
			//先获取网站国税局网站的SessionID,如果没有,请求网站获取一个.
			GsjSession sessionByWechatUserId = GsjSessionUtil
					.getSessionByWechatUserId(messageFromXML.getFromUserName());
			if (sessionByWechatUserId == null) {
				String sessionIDFromGsj = gsjService.getSessionIDFromGsj();
				GsjSession jSession = new GsjSession();
				jSession.setCreateTime(System.currentTimeMillis());
				jSession.setGsjSessionId(sessionIDFromGsj);
				GsjSessionUtil.setGsjSession(messageFromXML.getFromUserName(), jSession);
			}
			
			//拿着国税局网站的sessionID去请求验证码图片
			

			SampleBO basicGeneralUrl = aipOcrClient.basicGeneralUrl(messageFromXML.getPicUrl());
			List<Map<String, String>> words_result = basicGeneralUrl.getWords_result();
			if(words_result!=null&&words_result.size()>1){
				GsjSession jSession2 = GsjSessionUtil
				.getSessionByWechatUserId(messageFromXML.getFromUserName());
				jSession2.setFaPiaoDaiMa(words_result.get(0).get("words"));
				jSession2.setFaPiaoHaoMa(words_result.get(1).get("words"));
				
			}
			StringBuffer words = new StringBuffer();
			
			for (Map<String, String> map : words_result) {
				String string = map.get("words");
				words.append(string);
				words.append("\n");
				System.out.println(string);
			}

			result = generateResponse(messageFromXML, CommonUtil.MessageTypeImage, "MediaID的内容");

		}

		// 生成响应信息

		return result;
	}

	/**
	 * 生成响应信息
	 * 
	 * @param messageFromXML
	 * @param content
	 * @return
	 */
	private String generateResponse(WeChatCommunicationBO messageFromXML, String msgType, String content) {

		StringBuffer sb = new StringBuffer();
		sb.append("<xml>");
		sb.append("<ToUserName><![CDATA[").append(messageFromXML.getFromUserName()).append("]]></ToUserName>");
		sb.append("<FromUserName><![CDATA[").append(messageFromXML.getToUserName()).append("]]></FromUserName>");
		sb.append("<CreateTime>").append(System.currentTimeMillis()).append("</CreateTime>");
		sb.append("<MsgType><![CDATA[").append(msgType).append("]]></MsgType>");

		// 如果回复的消息是文字信息
		if (CommonUtil.MessageTypeText.equals(msgType)) {
			sb.append("<Content><![CDATA[").append(content).append("]]></Content>");
		}
		// 如果回复的消息是图片
		if (CommonUtil.MessageTypeImage.equals(msgType)) {
			sb.append("<MediaId><![CDATA[").append(content).append("]]></MediaId>");
		}

		sb.append("</xml>");
		return sb.toString();
	}

	/**
	 * 解析接收信息
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private WeChatCommunicationBO getMessageFromXML(HttpServletRequest request)
			throws IOException, ParserConfigurationException, SAXException {
		WeChatCommunicationBO result = new WeChatCommunicationBO();

		ServletInputStream inputStream = request.getInputStream();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder newDocumentBuilder = factory.newDocumentBuilder();
		org.w3c.dom.Document parse = newDocumentBuilder.parse(inputStream);
		org.w3c.dom.Element documentElement = parse.getDocumentElement();

		// 消息接收对象
		String toUserName = documentElement.getElementsByTagName(CommonUtil.ToUserName).item(0).getFirstChild()
				.getNodeValue();
		System.out.println("消息发给:" + toUserName);
		result.setToUserName(toUserName);
		// 消息来源
		String fromUserName = documentElement.getElementsByTagName(CommonUtil.FromUserName).item(0).getFirstChild()
				.getNodeValue();
		System.out.println("消息来源于:" + fromUserName);
		result.setFromUserName(fromUserName);
		// 消息发出时间
		String createTime = documentElement.getElementsByTagName(CommonUtil.CreateTime).item(0).getNodeValue();
		if (!StringUtils.isEmpty(createTime)) {
			Date date = new Date(Long.valueOf(createTime));
			SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd HH:MM:ss");
			System.out.println("消息发送于:" + format.format(date));
		}
		result.setCreateTime(createTime);
		// 消息ID
		String msgId = documentElement.getElementsByTagName(CommonUtil.MsgId).item(0).getNodeValue();
		System.out.println("消息唯一标识为:" + msgId);
		result.setMsgId(msgId);

		// 消息类型
		String msgType = documentElement.getElementsByTagName(CommonUtil.MsgType).item(0).getFirstChild()
				.getNodeValue();
		System.out.println("消息类型为:" + msgType);
		result.setMsgType(msgType);
		// 如果请求消息类型是文字信息
		if (CommonUtil.MessageTypeText.equals(msgType)) {
			// 消息内容
			String content = documentElement.getElementsByTagName(CommonUtil.Content).item(0).getFirstChild()
					.getNodeValue();
			System.out.println("消息内容为:" + content);
		}
		// 如果请求消息类型是图片类型
		if (CommonUtil.MessageTypeImage.equals(msgType)) {
			// 图片地址
			String picUrl = documentElement.getElementsByTagName(CommonUtil.PicUrl).item(0).getFirstChild()
					.getNodeValue();
			result.setPicUrl(picUrl);
			// 媒体ID
			NodeList elementsByTagName = documentElement.getElementsByTagName(CommonUtil.MediaId);
			if (elementsByTagName != null) {
				String MediaId = elementsByTagName.item(0).getFirstChild().getNodeValue();
				result.setMediaId(MediaId);
			}
		}
		return result;
	}
	// signature 微信加密签名
	// timestamp 时间戳
	// nonce 随机数
	// echostr 随机字符串
	// ToUserName:gh_fd51105af2c4
	// FromUserName:oZDsN1KlzzRBL2PUg73v91AT51-M
	// CreateTime:1528643172
	// MsgType:image
	// PicUrl:http://mmbiz.qpic.cn/mmbiz_jpg/5w0BycacbtnHyticlFjluKX0ELqnx12CzH2vPenyLhgVzEnzibdbdqYsfF9GfqYpcibX7FpbDxVQLibr9icosw648ow/0
	// MsgId:6565472431439525387
	// MediaId:JJC88OA0Y9Hd8V6zrwicYDXwU8_Gau82txF0WzpaMnBaSLSNT_MaYQIRH3ekPBP1
}

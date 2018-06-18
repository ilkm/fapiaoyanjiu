package com.taikang.jkx.controller;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.alibaba.fastjson.JSONObject;
import com.taikang.jkx.bo.CaptchaBO;
import com.taikang.jkx.bo.CommonUtil;
import com.taikang.jkx.bo.GsjSession;
import com.taikang.jkx.bo.WeChatCommunicationBO;
import com.taikang.jkx.inteface.GSJService;
import com.taikang.jkx.inteface.WechatService;
import com.taikang.jkx.thread.CaptchaThread;
import com.taikang.jkx.thread.CheckThread;
import com.taikang.jkx.thread.OcrThread;
import com.taikang.jkx.util.GsjSessionUtil;

@RestController
public class WXController {

	@Autowired
	private GSJService gsjService;
	@Autowired
	private WechatService wechatService;
	
	private Logger log = LoggerFactory.getLogger(WXController.class);

	@PostMapping("/wx")
	public String hello(HttpServletRequest request, String signature, String timestamp, int nonce, String echostr)
			throws IOException, JDOMException, ParserConfigurationException, SAXException {
		String result = "";
		// 解析消息内容
		WeChatCommunicationBO realUserMessage = getMessageFromXML(request);
		
		String jsonString = JSONObject.toJSONString(realUserMessage);
		WeChatCommunicationBO commonUserMessage = JSONObject.parseObject(jsonString, WeChatCommunicationBO.class);
		commonUserMessage.setFromUserName(GsjSessionUtil.COMMON_USER_ID);
		// 根据消息内容调用逻辑
		//如果是文字信息
		if (CommonUtil.MessageTypeText.equals(realUserMessage.getMsgType())) {
			//如果文字信息长度为4个字符,那么上传到是验证码信息
			if(realUserMessage.getContent().length()==4){
				result = generateResponse(realUserMessage, CommonUtil.MessageTypeText, "正在查验请稍后....");
				new Thread(new CheckThread(realUserMessage)).start();
			}else if(CommonUtil.REQUEST_MESSAGE_JG.equals(realUserMessage.getContent())){
				//如果文字内容为jg,那么认为要查看查询结果
				GsjSession sessionByWechatUserId = GsjSessionUtil.getSessionByWechatUserId(realUserMessage.getFromUserName());
				String resultMessage = sessionByWechatUserId.getResult();
				result = generateResponse(realUserMessage, CommonUtil.MessageTypeText, resultMessage);
			}else{
				//默认返回发送的文字信息
				result = generateResponse(realUserMessage, CommonUtil.MessageTypeText, realUserMessage.getContent());
			}
		}
		// 如果发送的是图片信息给用户返回一个验证码,并异步调用文字识别接口进行文字识别，
		if (CommonUtil.MessageTypeImage.equals(realUserMessage.getMsgType())) {
			//先想国税局网站请求sessionID信息
			GsjSession sessionIDFromGsj = gsjService.getSessionIDFromGsj(commonUserMessage.getFromUserName());
			//=================修改为不是每次都去请求,如果没有有效的验证码再去请求
			if(StringUtils.isEmpty(sessionIDFromGsj.getYanzhengma())){
				result = generateResponse(realUserMessage, CommonUtil.MessageTypeText, "正在生成验证码请稍后...");
				new Thread(new CaptchaThread()).start();
				
			}else{
				result = generateResponse(realUserMessage, CommonUtil.MessageTypeText, "正在查验请稍后....");
			}
			//将发票信息上传到百度云进行文字识别
			new Thread(new OcrThread(commonUserMessage,realUserMessage.getFromUserName())).start();
		}
		log.info(result);
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
			sb.append("<Image><MediaId><![CDATA[").append(content).append("]]></MediaId></Image>");
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
			result.setContent(content);
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

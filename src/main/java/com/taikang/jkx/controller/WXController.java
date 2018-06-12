package com.taikang.jkx.controller;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.JDOMException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import com.baidu.aip.ocr.AipOcr;
import com.taikang.jkx.bo.SampleBO;

@RestController
public class WXController {
	
	// 设置APPID/AK/SK
		public static final String APP_ID = "11376311";
		public static final String API_KEY = "n1mi18uaLVCfQSMSnQ17U4YQ";
		public static final String SECRET_KEY = "Dl17SeG2CqNTgLGpeY5zqzGikBqpbYf5";
	
	@PostMapping("/wx")
	public String hello(HttpServletRequest request,String signature,String timestamp,int nonce,String echostr) throws IOException, JDOMException, ParserConfigurationException, SAXException{
		
		// 初始化一个OcrClient
		AipOcr client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);
		// 可选：设置网络连接参数
		client.setConnectionTimeoutInMillis(2000);
		client.setSocketTimeoutInMillis(60000);
		
		ServletInputStream inputStream = request.getInputStream();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder newDocumentBuilder = factory.newDocumentBuilder();
		org.w3c.dom.Document parse = newDocumentBuilder.parse(inputStream);
		org.w3c.dom.Element documentElement = parse.getDocumentElement();
		
		
		//消息接收对象
		String toUserName = documentElement.getElementsByTagName("ToUserName").item(0).getFirstChild().getNodeValue();
		System.out.println("消息发给:"+toUserName);
		//消息来源
		String fromUserName = documentElement.getElementsByTagName("FromUserName").item(0).getFirstChild().getNodeValue();
		System.out.println("消息来源于:"+fromUserName);
		//消息发出时间
		String createTime = documentElement.getElementsByTagName("CreateTime").item(0).getNodeValue();
		if(!StringUtils.isEmpty(createTime)){
			Date date = new Date(Long.valueOf(createTime));
			SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd HH:MM:ss");
			System.out.println("消息发送于:"+format.format(date));
		}
		//消息ID
		String msgId = documentElement.getElementsByTagName("MsgId").item(0).getNodeValue();
		System.out.println("消息唯一标识为:"+msgId);
		StringBuffer sb = new StringBuffer();
		sb.append("<xml>");
		sb.append("<ToUserName><![CDATA[").append(fromUserName).append("]]></ToUserName>");
		sb.append("<FromUserName><![CDATA[").append(toUserName).append("]]></FromUserName>");
		sb.append("<CreateTime>").append(System.currentTimeMillis()).append("</CreateTime>");
		//消息类型
		String msgType = documentElement.getElementsByTagName("MsgType").item(0).getFirstChild().getNodeValue();
		System.out.println("消息类型为:"+msgType);
		sb.append("<MsgType><![CDATA[").append("text").append("]]></MsgType>");
		if(!StringUtils.isEmpty(msgType)&&msgType.equals("text")){
			//消息内容
			String content = documentElement.getElementsByTagName("Content").item(0).getFirstChild().getNodeValue();
			System.out.println("消息内容为:"+content);
			sb.append("<Content><![CDATA[").append(content).append("]]></Content>");
		}
		if(!StringUtils.isEmpty(msgType)&&msgType.equals("image")){
			
			//消息内容
			String picUrl = documentElement.getElementsByTagName("PicUrl").item(0).getFirstChild().getNodeValue();

			// 调用通用识别接口
			 JSONObject genRes = client.basicGeneralUrl(picUrl, new HashMap<String, String>());
			// 调用通用识别接口(含有位置信息)
			 //	JSONObject genRes = client.general(genFilePath, new HashMap<String, String>());
			 SampleBO parseObject = com.alibaba.fastjson.JSONObject.parseObject(genRes.toString(), SampleBO.class);
			
			//打印
			List<Map<String,String>> words_result = parseObject.getWords_result();
			StringBuffer words = new StringBuffer();
			for (Map<String, String> map : words_result) {
				String string = map.get("words");
				words.append(string);
				words.append("\n");
				System.out.println(string);
			}
			sb.append("<Content><![CDATA[").append(words.toString()).append("]]></Content>");
		}
		sb.append("</xml>");
		return sb.toString();
	}
//	signature 微信加密签名 
//	timestamp 时间戳 
//	nonce 随机数 
//	echostr 随机字符串 
//	ToUserName:gh_fd51105af2c4
//	FromUserName:oZDsN1KlzzRBL2PUg73v91AT51-M
//	CreateTime:1528643172
//	MsgType:image
//	PicUrl:http://mmbiz.qpic.cn/mmbiz_jpg/5w0BycacbtnHyticlFjluKX0ELqnx12CzH2vPenyLhgVzEnzibdbdqYsfF9GfqYpcibX7FpbDxVQLibr9icosw648ow/0
//	MsgId:6565472431439525387
//	MediaId:JJC88OA0Y9Hd8V6zrwicYDXwU8_Gau82txF0WzpaMnBaSLSNT_MaYQIRH3ekPBP1
}

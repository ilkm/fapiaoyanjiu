package com.taikang.jkx.thread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.util.StringUtils;

import com.taikang.jkx.bo.GsjSession;
import com.taikang.jkx.bo.SampleBO;
import com.taikang.jkx.bo.WeChatCommunicationBO;
import com.taikang.jkx.inteface.AipOcrClientService;
import com.taikang.jkx.inteface.GSJService;
import com.taikang.jkx.util.ApplicationContextHolder;
import com.taikang.jkx.util.GsjSessionUtil;

/**
 * 异步执行文字识别的类
 * @author zhangqh27
 *
 */
public class OcrThread implements Runnable {
	
	private WeChatCommunicationBO messageFromXML;
	private String userId;
	private AipOcrClientService aipOcrClient;
	private CloseableHttpClient httpClient;
	private GsjSession commonSession;
	private GSJService gsjService;

	public OcrThread(WeChatCommunicationBO wc,String userId){
		this.messageFromXML = wc;
		this.aipOcrClient = ApplicationContextHolder.getApplicationContext().getBean(AipOcrClientService.class);
		this.httpClient = ApplicationContextHolder.getApplicationContext().getBean(CloseableHttpClient.class);
		this.commonSession = null;
		this.gsjService =  ApplicationContextHolder.getApplicationContext().getBean(GSJService.class);
		this.userId = userId;
	}
	
	@Override
	public void run() {
		
		String picUrl = messageFromXML.getPicUrl();
		HttpGet request = new HttpGet(picUrl);
		String picName;
		String filename = "";
		try {
			//将上传的发票信息存储到本地
			CloseableHttpResponse execute = httpClient.execute(request);
			InputStream content = execute.getEntity().getContent();
			picName = UUID.randomUUID().toString().replace("-", "");
			FileOutputStream ops = new FileOutputStream(new File(picName+".jpg"));
			byte[] temp = new byte[1024];
			int read = 0;
			while((read = content.read(temp))>0){
				ops.write(temp, 0, read);
			}
			ops.close();
			//使用opencv技术将图片二值化
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			Mat mat = Imgcodecs.imread(picName+".jpg");
			Core.inRange(mat, new Scalar(0,0,0), new Scalar(100,100,100), mat);
			filename = UUID.randomUUID().toString().replace("-", "");
			filename = filename+".jpg";
			Imgcodecs.imwrite(filename, mat);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//调用ocr接口进行发票号码识别
		SampleBO basicGeneralUrl = aipOcrClient.basicGeneral(filename);
		List<Map<String, String>> words_result = basicGeneralUrl.getWords_result();
		if(words_result!=null&&words_result.size()>1){
			GsjSession userSession = GsjSessionUtil
					.getSessionByWechatUserId(userId);
			if(userSession == null){
				userSession = new GsjSession();
				GsjSessionUtil.setGsjSession(userId, userSession);
			}
			//TODO 待优化的点.如果识别出的结果比要的文字字数多,要能区分哪些是需要的信息哪些不是.
			String fpdm = words_result.get(0).get("words");
			String fphm = words_result.get(1).get("words");
			userSession.setFaPiaoDaiMa(fpdm);
			userSession.setFaPiaoHaoMa(fphm);
			
			try {
				String md5v = gsjService.getMd5v(fpdm, fphm, GsjSessionUtil.getSessionByWechatUserId(messageFromXML.getFromUserName()).getGsjSessionId());
				userSession.setMd5v(md5v);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//如果common用户对应的session中已经存在验证码信息,则直接请求国税局网站查询验旧日期
		if(!StringUtils.isEmpty(commonSession.getYanzhengma())){
			try {
				String checkResult = gsjService.check(commonSession, commonSession.getYanzhengma());
				GsjSession userSession = GsjSessionUtil.getSessionByWechatUserId(userId);
				userSession.setResult(checkResult);
			} catch (Exception e) {
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

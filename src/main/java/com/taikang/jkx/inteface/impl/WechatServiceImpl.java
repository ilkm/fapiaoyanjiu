package com.taikang.jkx.inteface.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.taikang.jkx.bo.CaptchaBO;
import com.taikang.jkx.inteface.WechatService;
import com.taikang.jkx.util.AccessTokenManager;
import com.taikang.jkx.util.HttpClientCreator;

/**
 * 与微信公众平台交互的接口
 * @author zhangqh27
 *
 */
@Service
public class WechatServiceImpl implements WechatService {
	
	@Value("${wechat.uploadMediaUrl}")
	private String uploadUrl;
	@Autowired
	private HttpClientCreator httpClientCreator;
	@Autowired
	private AccessTokenManager accessTokenManager;

	@Override
	public String uploadTempMedia(CaptchaBO captchaBo) throws IOException {
		
		CloseableHttpClient httpClient = httpClientCreator.getHttpClient();
		String replace = uploadUrl.replace("ACCESS_TOKEN_aaa", accessTokenManager.getAccessToken());
		HttpPost httpPost = new HttpPost(replace);
		
		
		String randomFileName = UUID.randomUUID().toString().replace("-", "")+".jpg";
		StringBody fileName = new StringBody(randomFileName, ContentType.TEXT_PLAIN );
		StringBody fileLength = new StringBody(""+captchaBo.getContentLength(), ContentType.TEXT_PLAIN);
		StringBody contentType = new StringBody(captchaBo.getContentType(), ContentType.TEXT_PLAIN);
		
		File file = new File(randomFileName);
		OutputStream output = new FileOutputStream(file);
		InputStream inputStream = captchaBo.getInputStream();
		byte[] bs = new byte[2048];
		int i = 0;
		while((i=inputStream.read(bs))>0){
			output.write(bs, 0, i);
		}
		output.close();
		FileBody fileBody = new FileBody(file);
		
		HttpEntity entity = MultipartEntityBuilder.create()
			.addPart("media", fileBody)
			.addPart("filename", fileName)
			.addPart("filelength",fileLength)
			.addPart("content-type", contentType)
			.build();
		
		httpPost.setEntity(entity);
		CloseableHttpResponse execute = httpClient.execute(httpPost);
		
		HttpEntity entity2 = execute.getEntity();
		
		InputStream content = entity2.getContent();
		InputStreamReader reader = new InputStreamReader(content);
		char[] temp = new char[2048];
		int read = reader.read(temp);
		String str = new String(temp,0,read);
		
		JSONObject json = JSONObject.parseObject(str);
		System.out.println(json.toJSONString());
		//{"type":"TYPE","media_id":"MEDIA_ID","created_at":123456789}
		String mediaId = json.getString("media_id");
		System.out.println(mediaId);
		return mediaId;
	}

}

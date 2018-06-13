package com.taikang.jkx.inteface.impl;

import java.util.HashMap;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.baidu.aip.ocr.AipOcr;
import com.taikang.jkx.bo.SampleBO;
import com.taikang.jkx.inteface.AipOcrClientService;

@Service
public class AipOcrClientServiceImpl implements AipOcrClientService {

	// 设置APPID/AK/SK
	public static final String APP_ID = "11376311";
	public static final String API_KEY = "n1mi18uaLVCfQSMSnQ17U4YQ";
	public static final String SECRET_KEY = "Dl17SeG2CqNTgLGpeY5zqzGikBqpbYf5";

	@Override
	public SampleBO basicGeneralUrl(String picUrl) {
		// 初始化一个OcrClient
		AipOcr client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);
		// 可选：设置网络连接参数
		client.setConnectionTimeoutInMillis(2000);
		client.setSocketTimeoutInMillis(60000);
		// 调用通用识别接口
		JSONObject genRes = client.basicGeneralUrl(picUrl, new HashMap<String, String>());
		// 调用通用识别接口(含有位置信息)
		// JSONObject genRes = client.general(genFilePath, new
		// HashMap<String, String>());
		SampleBO parseObject = com.alibaba.fastjson.JSONObject.parseObject(genRes.toString(), SampleBO.class);
		return parseObject;
	}

}

package com.taikang.jkx.inteface;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.taikang.jkx.bo.CaptchaBO;

public interface WechatService {
	
	public String uploadTempMedia(CaptchaBO captchaBo) throws UnsupportedEncodingException, IOException;

}

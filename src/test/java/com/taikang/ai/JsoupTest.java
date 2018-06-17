package com.taikang.ai;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import com.alibaba.fastjson.JSONObject;

public class JsoupTest {
	
	@Test
	public void fun1(){
		Document parse = Jsoup.parse("<script language=\"JavaSCRIPT\">alert(\"验证失败，不能进入系统!!\");window.opener=null;window.open('cx_sgfplxcx.jsp','_self');window.close();</script>");
		Element result = parse.selectFirst("script");
		List<DataNode> dataNodes = result.dataNodes();
		System.out.println(dataNodes);
		System.out.println(JSONObject.toJSONString(result));
	}

}

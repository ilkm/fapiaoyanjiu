package com.taikang.jkx.inteface.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.taikang.jkx.bo.CaptchaBO;
import com.taikang.jkx.bo.GsjSession;
import com.taikang.jkx.inteface.GSJService;
import com.taikang.jkx.util.GsjSessionUtil;
import com.taikang.jkx.util.HttpClientCreator;


/**
 * 实现操作国税局网站的相关操作
 * 
 * @author zhangqh27
 *
 */
@Service
public class GSJServiceImpl implements GSJService {
	

	@Autowired
	private HttpClientCreator httpClinetCreator;

	@Value("${gsj.gsjUrl}")
	private String gsjUrl;
	@Value("${gsj.captchaUrl}")
	private String captchaUrl;
	@Value("${gsj.md5Url}")
	private String md5Url;
	@Value("${gsj.yanjiuUrl}")
	private String yanjiuUrl;

	@Value("${gsj.sessionIdExpireTime}")
	private long gsjSessionExpireTime;
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public GsjSession getSessionIDFromGsj(String userId) throws ClientProtocolException, IOException {

		// 先判断本地系统中存储的sessionID是否已过期，如果过期了就移除。
		log.info("国税局session过期时间为:"+gsjSessionExpireTime);
		GsjSessionUtil.expireGsjSesionByUserId(userId, gsjSessionExpireTime);
		// 先从数据库中查看当前微信用户是否已存在sessionID。,如果没有,请求网站获取一个.
		GsjSession sessionByWechatUserId = GsjSessionUtil.getSessionByWechatUserId(userId);
		if (sessionByWechatUserId != null) {
			log.info("执行国税局session清理之后,我存活下来了");
			return sessionByWechatUserId;
		}
		String JSESSIONID = "";
		// 获取客户端
		CloseableHttpClient client = httpClinetCreator.getHttpClient();

		// 创建get请求地址
		HttpGet get = new HttpGet(gsjUrl);
		HttpResponse execute = client.execute(get);
		// 获取响应状态码
		StatusLine statusLine = execute.getStatusLine();
		log.debug(statusLine.toString());
		// 获取session识别码
		// Set-Cookie:JSESSIONID=o9LyqpSYohchmBTjr9_60L7LVScagBuhVYRkT1XkDr-DsysRyK-w!-1834616633;
		// path=/; HttpOnly
		Header cookieHeader = execute.getFirstHeader("Set-Cookie");
		if (cookieHeader != null) {
			String[] split = cookieHeader.getValue().split(";");
			if (split != null && split.length > 0) {
				String jsessionIdStr = split[0];
				String[] jsessionIdPeers = jsessionIdStr.split("=");
				if (jsessionIdPeers != null && jsessionIdPeers.length > 1) {
					JSESSIONID = jsessionIdPeers[1];
				}
			}
		}

		GsjSession jSession = new GsjSession();
		jSession.setCreateTime(System.currentTimeMillis());
		jSession.setGsjSessionId(JSESSIONID);
		log.debug("请求了一次sessionID,sessionID为:{}",JSESSIONID);
		GsjSessionUtil.setGsjSession(userId, jSession);
		return jSession;
	}

	@Override
	public CaptchaBO getCaptchaBySessionID(String sessionID) throws ClientProtocolException, IOException {
		CloseableHttpClient httpClient = httpClinetCreator.getHttpClient();
		// 创建get请求地址
		HttpGet get = new HttpGet(captchaUrl);
		get.setHeader("Cookie", "JSESSIONID=" + sessionID);
		HttpResponse execute = httpClient.execute(get);
		HttpEntity entity = execute.getEntity();
		InputStream content = entity.getContent();
		String contentType = entity.getContentType().getValue();
		long contentLength = entity.getContentLength();

		CaptchaBO captcha = new CaptchaBO();
		captcha.setInputStream(content);
		captcha.setContentLength(contentLength);
		captcha.setContentType(contentType);
		return captcha;
	}

	/**
	 * 获取验旧前的md5校验码
	 */
	@Override
	public String getMd5v(String fpdm, String fphm,String sessionId) throws ClientProtocolException, IOException {
		CloseableHttpClient httpClient = httpClinetCreator.getHttpClient();
		String requestUrl = "https://59.173.248.30:7013/include1/fpcxjm.jsp?str1=" + "&str2=" + fpdm + "&str3=" + fphm;
		HttpGet get = new HttpGet(requestUrl);
		get.addHeader("Cookie", "JSESSIONID=" + sessionId);
		CloseableHttpResponse response = httpClient.execute(get);
		InputStream content = response.getEntity().getContent();
		char[] response_chars = new char[1024];
		InputStreamReader reader = new InputStreamReader(content);
		int read = reader.read(response_chars);
		String result = new String(response_chars, 0, read);
		return result;
	}
	
	/**
	 * 向国税局网站提交请求信息
	 * @param sessionByWechatUserId
	 * @param content
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	@Override
	public String check(GsjSession sessionByWechatUserId, String content,String sessionID) throws ClientProtocolException, IOException {
		long startTime = System.currentTimeMillis();
		System.out.println(JSONObject.toJSONString(sessionByWechatUserId));
		//通过httpClient请求
		CloseableHttpClient httpClient = httpClinetCreator.getHttpClient();
		long httpClientGetTime = System.currentTimeMillis();
		log.info("获取连接的时间是{}秒",(httpClientGetTime-startTime)/1000);
		HttpPost post = new HttpPost(yanjiuUrl);
		List<NameValuePair> parameters = new ArrayList<>();
		NameValuePair cxbz = new BasicNameValuePair("cxbz", "lscx");
		NameValuePair fpdm = new BasicNameValuePair("fpdm", sessionByWechatUserId.getFaPiaoDaiMa());
		log.debug("fpdm:{}",sessionByWechatUserId.getFaPiaoDaiMa());
		NameValuePair fphm = new BasicNameValuePair("fphm", sessionByWechatUserId.getFaPiaoHaoMa());
		log.debug("fphm:{}",sessionByWechatUserId.getFaPiaoHaoMa());
		NameValuePair je = new BasicNameValuePair("je", "10");
		NameValuePair kaptchafield = new BasicNameValuePair("kaptchafield", content);
		NameValuePair kjfsbh = new BasicNameValuePair("kjfsbh", "");
		NameValuePair md5v = new BasicNameValuePair("md5v", sessionByWechatUserId.getMd5v());
		NameValuePair rq = new BasicNameValuePair("rq", "20180506");
		NameValuePair ywlx = new BasicNameValuePair("ywlx", "FPCX_LXCX");
		NameValuePair ywlxbf = new BasicNameValuePair("ywlxbf", "FPCX_LXCX");

		parameters.add(cxbz);
		parameters.add(fpdm);
		parameters.add(fphm);
		parameters.add(je);
		parameters.add(kaptchafield);
		parameters.add(kjfsbh);
		parameters.add(md5v);
		parameters.add(rq);
		parameters.add(ywlx);
		parameters.add(ywlxbf);
		
		HttpEntity requestEntity = new UrlEncodedFormEntity(parameters);
		post.setEntity(requestEntity);
		//将包含sessionID的JSESSIONID作为请求头的一部分
		post.addHeader("Cookie", "JSESSIONID=" + sessionID);
		log.debug("请求使用的sessionID为:{}",sessionID);
		post.addHeader("Referer	", "https://59.173.248.30:7013/include1/cx_sgfplxcx.jsp");
		CloseableHttpResponse response = httpClient.execute(post);
		long executeEndTime = System.currentTimeMillis();
		log.info("请求完成后的时间为:{}秒",(executeEndTime-startTime)/1000);
		HttpEntity responseEntity = response.getEntity();
		InputStream content2 = responseEntity.getContent();
//		log.debug(responseEntity.getContentLength()+"");
		StringBuffer sb = new StringBuffer();
		char[] charTemp = new char[1024];
		int readLenth = 0;
		InputStreamReader reader = new InputStreamReader(content2);
		while((readLenth = reader.read(charTemp))>0){
			sb.append(charTemp, 0, readLenth);
		}
		long parseBefore = System.currentTimeMillis();
		log.info("解析结果前的时间为:{}秒",(parseBefore-startTime)/1000);
		Document parse = Jsoup.parse(sb.toString());
		long parseEnd = System.currentTimeMillis();
		log.info("解析完成后的时间为:{}秒",(parseEnd-startTime)/1000);
		Element result = parse.selectFirst("td[class=red_12]");
		if(result==null){
			result = parse.selectFirst("script");
			if(result!=null){
				log.debug("验证结果中包含script脚本");
				List<DataNode> dataNodes = result.dataNodes();
				DataNode dataNode = dataNodes.get(0);
				String wholeData = dataNode.getWholeData();
				return wholeData;
			}
		}
		if(result == null){
			result = parse.selectFirst("a[href=cx_sgfplxcx.jsp]").selectFirst("font");
		}
		long endTime = System.currentTimeMillis();
		log.info("check方法完成时间为:{}秒",(endTime-startTime)/1000);
		return result.text();
	}
}

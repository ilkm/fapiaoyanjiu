package com.taikang.jkx.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.taikang.jkx.bo.GsjSession;

/**
 * 用于操作国税局网站的sessionID
 * 
 * @author zhangqh27
 *
 */
public class GsjSessionUtil {

	public static Map<String, GsjSession> gsjSessions = new HashMap<>();

	public GsjSession getSessionByWechatUserId(String userId) {
		return gsjSessions.get(userId);
	}

	public void setGsjSession(String userId, GsjSession gsjSession) {
		gsjSessions.put(userId, gsjSession);
	}

	/**
	 * 删除过期的国税局session信息
	 */
	public void expireGsjSession(long expireTime) {
		Iterator<Entry<String, GsjSession>> iterator = gsjSessions.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, GsjSession> next = iterator.next();
			GsjSession value = next.getValue();
			if (System.currentTimeMillis() - value.getCreateTime() > expireTime) {
				iterator.remove();
			}
		}
	}
}

package com.taikang.jkx.bo;


/**
 * 记录国税局网站的sessionID
 * @author zhangqh27
 *
 */
public class GsjSession {
	
	
	
	private String gsjSessionId;
	
	private long createTime;
	
	private String faPiaoDaiMa;
	private String faPiaoHaoMa;

	public String getFaPiaoDaiMa() {
		return faPiaoDaiMa;
	}

	public void setFaPiaoDaiMa(String faPiaoDaiMa) {
		this.faPiaoDaiMa = faPiaoDaiMa;
	}

	public String getFaPiaoHaoMa() {
		return faPiaoHaoMa;
	}

	public void setFaPiaoHaoMa(String faPiaoHaoMa) {
		this.faPiaoHaoMa = faPiaoHaoMa;
	}

	public String getGsjSessionId() {
		return gsjSessionId;
	}

	public void setGsjSessionId(String gsjSessionId) {
		this.gsjSessionId = gsjSessionId;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

}

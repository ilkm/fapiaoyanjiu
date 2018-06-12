package com.taikang.jkx.bo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 调用通用识别接口返回实体
 * @author Administrator
 *
 */
public class SampleBO {

	private String log_id;
	private int words_result_num;
	private List<Map<String, String>> words_result = new ArrayList<Map<String, String>>();

	public String getLog_id() {
		return log_id;
	}

	public void setLog_id(String log_id) {
		this.log_id = log_id;
	}

	public int getWords_result_num() {
		return words_result_num;
	}

	public void setWords_result_num(int words_result_num) {
		this.words_result_num = words_result_num;
	}

	public List<Map<String, String>> getWords_result() {
		return words_result;
	}

	public void setWords_result(List<Map<String, String>> words_result) {
		this.words_result = words_result;
	}

}

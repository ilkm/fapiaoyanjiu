package com.taikang.ai;

import org.junit.Test;
import org.springframework.util.StringUtils;

public class CommonTest {
	
	@Test
	public void fun1(){
		String strs = "432";
		fun2(strs);
		System.out.println(strs);
	}
	
	private void fun2(String str){
		str = "123";
	}
	
	@Test
	public void fun3(){
		String str1 = null;
		String str2 = "";
		System.out.println(StringUtils.isEmpty(str1));
		System.out.println(StringUtils.isEmpty(str2));
	}

}

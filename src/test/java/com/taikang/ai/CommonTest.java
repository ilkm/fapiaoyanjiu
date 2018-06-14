package com.taikang.ai;

import org.junit.Test;

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

}

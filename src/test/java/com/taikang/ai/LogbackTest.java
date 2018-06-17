package com.taikang.ai;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogbackTest {
	Logger log = LoggerFactory.getLogger(LogbackTest.class);
	
	@Test
	public void fun1(){
		log.info("测试log占位符,现在时间是{}年{}月{}日","2018","06","17");
	}

}

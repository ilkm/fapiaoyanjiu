package com.taikang.ai;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	@Test
	public void fun4(){
		String word = "123456789251";
		boolean matches = word.matches("[^0-9]*[0-9]{12}[^0-9]*");
		boolean matches2 = word.matches("[0-9]{12}");
		System.out.println(word+"->[^0-9]*[0-9]{12}[^0-9]*:"+matches);
		System.out.println(word+"->[0-9]{12}:"+matches2);
	}
	
	@Test
	public void fun5(){
		String word = "@123456789251#";
		boolean matches = word.matches("[^0-9]*[0-9]{12}[^0-9]*");
		boolean matches2 = word.matches("[0-9]{12}");
		System.out.println(word+"->[^0-9]*[0-9]{12}[^0-9]*:"+matches);
		System.out.println(word+"->[0-9]{12}:"+matches2);
	}
	
	@Test
	public void fun6(){
		String word = "31234567892515";
		boolean matches = word.matches("[^0-9]*[0-9]{12}[^0-9]*");
		boolean matches2 = word.matches("[0-9]{12}");
		System.out.println(word+"->[^0-9]*[0-9]{12}[^0-9]*:"+matches);
		System.out.println(word+"->[0-9]{12}:"+matches2);
	}
	
	@Test
	public void fun7(){
	    String regex = ".*[^0-9]*[0-9]{12}[^0-9]*.*";
	    String subStrRegex = "[0-9]{12}";
		String word = "24535@123456789251#36546";
		boolean matches = word.matches(regex);
		System.out.println(matches);
		Pattern compile = Pattern.compile(subStrRegex);
		Matcher matcher = compile.matcher(word);
		if(matcher.find()){
			String group = matcher.group();
			System.out.println(group);
		}
	}
	@Test
	public void fun8(){
		String word = "烈04197881";
		//烈04197881
		String fphmRegex = ".*[^0-9]*[0-9]{8}[^0-9]*.*";
		String fphmSubRegex = "[0-9]{8}";
		Pattern compile = Pattern.compile(fphmSubRegex);
		Matcher matcher = compile.matcher(word);
		if(matcher.find()){
			String fphm = matcher.group();
			System.out.println(fphm);
		}
	}
}

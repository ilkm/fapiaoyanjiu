package com.taikang.jkx.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogAspect {
	Logger log = LoggerFactory.getLogger(LogAspect.class);
	
	@Pointcut("execution(public * com.taikang.jkx..*.*(..))")
	public void timeLogStub(){}
	
	@Around("timeLogStub()")
	public Object around(ProceedingJoinPoint pjp){
		Class<? extends Object> class1 = pjp.getTarget().getClass();
		MethodSignature signature = (MethodSignature) pjp.getSignature();
		long startTime = System.currentTimeMillis();
		try {
			Object proceed = pjp.proceed();
			long endTime = System.currentTimeMillis();
			log.info("{}类的{}方法共花费了{}秒",class1.getName(),signature.getName(),(endTime-startTime)/1000);
			return proceed;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

}

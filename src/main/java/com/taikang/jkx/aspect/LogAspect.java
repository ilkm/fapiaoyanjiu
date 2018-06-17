package com.taikang.jkx.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
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
	
	@Pointcut("execution(public * com.taikang.jkx.*.*(..))")
	public void logStub(){}
	
	@Around("logStub()")
	public Object around(ProceedingJoinPoint pjp){
		Class<? extends ProceedingJoinPoint> class1 = pjp.getClass();
		MethodSignature signature = (MethodSignature) pjp.getSignature();
		long startTime = System.currentTimeMillis();
		log.debug("{}开始执行{}的{}方法",startTime,class1.getName(),signature.getName());
		try {
			Object proceed = pjp.proceed();
			long endTime = System.currentTimeMillis();
			log.debug("{}{}的{}方法执行完毕",endTime,class1.getName(),signature.getName());
			log.debug("{}方法共花费了{}秒",signature.getName(),(endTime-startTime)/1000);
			return proceed;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

}

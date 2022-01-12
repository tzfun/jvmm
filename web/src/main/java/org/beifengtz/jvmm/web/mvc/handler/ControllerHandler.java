package org.beifengtz.jvmm.web.mvc.handler;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Description: TODO
 *
 * Created in 16:06 2022/1/12
 *
 * @author beifengtz
 */
@Aspect
@Component
@Slf4j
public class ControllerHandler {

    @Value("${jvmm.log-controller}")
    private boolean logController;

    private long reqTime;

    @Pointcut("execution(public * org.beifengtz.jvmm.web.mvc.controller..*.*(..))")
    public void pointcut() {
        //do nothing just for filtering
    }

    @Before("pointcut()")
    public void reqBefore(JoinPoint joinPoint) {
        reqTime = System.currentTimeMillis();
    }

    @After("pointcut()")
    public void reqAfter(JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        if (logController && !request.getMethod().equalsIgnoreCase("OPTIONS")) {
            log.info("Request. uri:'{}', use: {} ms, params: {}", request.getRequestURI(), (System.currentTimeMillis() - reqTime), request.getQueryString());
        }
    }
}

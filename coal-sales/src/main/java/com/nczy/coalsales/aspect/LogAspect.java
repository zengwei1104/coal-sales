package com.nczy.coalsales.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nczy.coalsales.annotation.Log;
import com.nczy.coalsales.entity.SysLog;
import com.nczy.coalsales.mapper.SysLogMapper;
import com.nczy.coalsales.security.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * 操作日志切面：拦截标注了 {@link Log} 注解的方法，
 * 自动记录操作人、操作内容、方法、参数、IP、耗时、结果。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final SysLogMapper sysLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(logAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint, Log logAnnotation) throws Throwable {
        long start = System.currentTimeMillis();
        boolean success = true;
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            success = false;
            throw e;
        } finally {
            try {
                saveLog(joinPoint, logAnnotation, success, System.currentTimeMillis() - start);
            } catch (Exception ex) {
                // 日志记录失败不影响主流程，只打 warn
                log.warn("记录操作日志失败", ex);
            }
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, Log logAnnotation,
                         boolean success, long costTime) {
        SysLog sysLog = new SysLog();

        // 操作人
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            sysLog.setUserId(loginUser.getUserId());
            sysLog.setUsername(loginUser.getUsername());
        }

        sysLog.setOperation(logAnnotation.value());

        // 方法签名：类名.方法名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        sysLog.setMethod(signature.getDeclaringType().getSimpleName()
                + "." + signature.getName());

        // 参数（序列化为 JSON，忽略无法序列化的对象如 HttpServletRequest）
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                sysLog.setParams(objectMapper.writeValueAsString(
                        Arrays.stream(args)
                                .filter(a -> !(a instanceof HttpServletRequest))
                                .toArray()));
            }
        } catch (Exception e) {
            sysLog.setParams("参数序列化失败");
        }

        // IP
        sysLog.setIp(getClientIp());

        sysLog.setStatus(success ? 1 : 0);
        sysLog.setCostTime((int) costTime);

        sysLogMapper.insert(sysLog);
    }

    private String getClientIp() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

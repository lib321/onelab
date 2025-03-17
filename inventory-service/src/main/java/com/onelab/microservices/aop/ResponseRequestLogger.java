package com.onelab.microservices.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
@Component
@Aspect
public class ResponseRequestLogger {

    private final ObjectMapper objectMapper;

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object logHttpRequestResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request =
                ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        HttpServletResponse response =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();

        String ip = request.getRemoteAddr();
        String username = request.getHeader("Authorization");
        String requestBody = getRequestBody(joinPoint);

        log.info("Request: IP={}, User={}, Method={}, URI={}, Body={}",
                ip, username, request.getMethod(), request.getRequestURI(), requestBody);

        Object result;
        long startTime = System.currentTimeMillis();
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            log.error("Exception: IP={}, User={}, Method={}, URI={}, Error={}",
                    ip, username, request.getMethod(), request.getRequestURI(), e.getMessage());
            throw e;
        }
        long duration = System.currentTimeMillis() - startTime;

        String responseBody = convertObjectToJson(result);
        int status = response.getStatus();

        log.info("Response: IP={}, User={}, Method={}, URI={}, Status={}, Body={}, Time Taken={}ms",
                ip, username, request.getMethod(), request.getRequestURI(), status, responseBody, duration);

        return result;
    }

    private String getRequestBody(ProceedingJoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .map(this::convertObjectToJson)
                .reduce((arg1, arg2) -> arg1 + ", " + arg2)
                .orElse("");
    }

    private String convertObjectToJson(Object object) {
        if (object == null) return "";
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("JSON serialization error", e);
            return "Error serializing object";
        }
    }
}

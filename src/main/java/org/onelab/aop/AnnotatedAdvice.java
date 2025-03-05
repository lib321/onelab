package org.onelab.aop;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.Joinpoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.onelab.model.Orders;
import org.onelab.model.Users;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Aspect
@Slf4j
public class AnnotatedAdvice {

//    @Pointcut("execution(* org.onelab.service.AppService.save*(..))")
//    public void anySaveServiceMethod() {
//    }
//
//    @Pointcut("execution(* org.onelab.service.AppService.find*(..))")
//    public void anyFindServiceMethod() {
//    }
//
//    @Pointcut("execution(* org.onelab.repoimpl.UserRepoImpl.findById(..))")
//    public void findUserServiceMethod() {}
//
//    @Pointcut("execution(public * org.onelab.service..*(..))")
//    public void allPublicMethodsInServicePackage() {}
//
//    @Before("anyFindServiceMethod()")
//    public void beforeAnyServiceMethod() {
//        log.info("----- Before -----");
//    }
//
//    @Around("anySaveServiceMethod()")
//    public Object aroundAnyServiceMethod(ProceedingJoinPoint joinpoint) throws Throwable {
//        Object[] arguments = joinpoint.getArgs();
//        for (Object argument : arguments) {
//            if (argument instanceof Users) {
//                log.info("----- User saved: " + ((Users) argument).getFirstname());
//            } else if (argument instanceof Orders) {
//                log.info("----- Order saved: " + argument);
//            }
//        }
//        return joinpoint.proceed();
//    }
//
//    @Around("findUserServiceMethod()")
//    public Object handleFindById(ProceedingJoinPoint joinPoint) throws Throwable {
//        try {
//            return joinPoint.proceed();
//        } catch (EmptyResultDataAccessException ex) {
//            log.error("----- User not found -----");
//            return Optional.empty();
//        }
//    }
//
//
//    @AfterReturning(value = "anyFindServiceMethod()", returning = "result")
//    public void afterReturningResult(Object result) {
//        if (result instanceof List) {
//            log.info("----- After returning list of size: " + ((List<?>) result).size());
//        } else {
//            log.info("----- After returning object: " + result);
//        }
//    }
//
//    @AfterThrowing(value = "findUserServiceMethod()", throwing = "ex")
//    public void afterTrowingFindAny(EmptyResultDataAccessException ex) {
//        log.error("----- Error -----: {}", ex.getMessage());
//    }
//
//    @After("allPublicMethodsInServicePackage()")
//    public void afterAllPublicMethods() {
//        log.info("----- Method execution is done -----");
//    }
}

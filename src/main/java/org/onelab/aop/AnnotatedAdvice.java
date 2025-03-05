package org.onelab.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Aspect
@Slf4j
public class AnnotatedAdvice {

    @Pointcut("within(@org.springframework.transaction.annotation.Transactional *)")
    public void transactionalMethods() {
    }

    @Before("transactionalMethods()")
    public void beforeTransaction(JoinPoint joinpoint) {
        log.info("------------- Транзакция началась: " + joinpoint.getSignature().getName());
    }

    @AfterReturning(value = "transactionalMethods()", returning = "result")
    public void afterSuccessfulTransaction(JoinPoint joinPoint, Object result) {
        if (result instanceof List) {
            log.info("------------- Транзакция успешно завершена: " + joinPoint.getSignature().getName() +
                    ", результат: " + ((List<?>) result).size());
        } else {
            log.info("------------- Транзакция успешно завершена: " + joinPoint.getSignature().getName() +
                    ", результат: " + result);
        }
    }

    @AfterThrowing(value = "transactionalMethods()", throwing = "ex")
    public void afterTransactionException(JoinPoint joinPoint, Throwable ex) {
        log.error("------------- Ошибка в транзакции: " + joinPoint.getSignature().getName() +
                ", исключение: " + ex.getMessage());
    }

    @Around("transactionalMethods()")
    public Object aroundTransaction(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            return null;
        }
    }

    @After("transactionalMethods()")
    public void afterTransaction(JoinPoint joinPoint) {
        log.info("------------- Транзакция завершена: " + joinPoint.getSignature().getName());
    }
}

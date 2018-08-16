package io.coti.common.aspect;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class PoftDurationAspect {

    @Around("execution(* io.coti.common.services.PoftService.*(..))")
    public Object timerAspect(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTimeMs = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } finally {
            log.info("Pure poft Timing : {}(), took {} ms | inputs = {}",
                    joinPoint.getSignature().getName(),
                    NumberFormat.getInstance().format((System.currentTimeMillis() - startTimeMs)),
                    Arrays.toString(joinPoint.getArgs()));
        }
    }
}

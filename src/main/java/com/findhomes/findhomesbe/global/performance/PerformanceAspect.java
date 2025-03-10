package com.findhomes.findhomesbe.global.performance;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class PerformanceAspect {


    @Around("@annotation(com.findhomes.findhomesbe.global.performance.MeasurePerformance)")
    public Object measurePerformance(ProceedingJoinPoint joinPoint) throws Throwable {

        long startTime = System.currentTimeMillis();

        try {
            // 메서드 실행
            Object result = joinPoint.proceed();
            return result;
        } finally {
            long endTime = System.currentTimeMillis();
            long elapsedMillis = endTime - startTime;
            double elapsedSeconds = elapsedMillis / 1000.0;

            log.info("[성능 측정] {}.{} 실행 시간: {}초",
                    joinPoint.getTarget().getClass().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    elapsedSeconds);
        }
    }
}

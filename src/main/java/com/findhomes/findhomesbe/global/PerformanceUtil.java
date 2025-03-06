package com.findhomes.findhomesbe.global;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class PerformanceUtil {

    // 성능 측정할 함수가 반환값이 있을때
    public static <T> T measurePerformance(Supplier<T> supplier, String operationDescription) {
        long startTime = System.currentTimeMillis();
        T result = supplier.get();
        long endTime = System.currentTimeMillis();

        double elapsedTime = (endTime - startTime) / 1000.0;
        log.info(operationDescription + " 소요 시간: " + elapsedTime + "초");
        return result;
    }
    // 성능 측정할 함수가 반환값이 없을때
    public static void measurePerformance(Runnable runnable, String operationDescription) {
        long startTime = System.currentTimeMillis();
        runnable.run();
        long endTime = System.currentTimeMillis();

        double elapsedTime = (endTime - startTime) / 1000.0;
        log.info(operationDescription + " 소요 시간: " + elapsedTime + "초");
    }
}


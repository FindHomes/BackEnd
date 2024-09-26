package com.findhomes.findhomesbe.service;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;
import java.util.logging.Logger;
@Slf4j
public class PerformanceUtil {
    public static <T> T measurePerformance(Supplier<T> supplier, String operationDescription) {
        long startTime = System.currentTimeMillis();
        T result = supplier.get();
        long endTime = System.currentTimeMillis();

        double elapsedTime = (endTime - startTime) / 1000.0;
        log.info(operationDescription + " 소요 시간: " + elapsedTime + "초");
        return result;
    }

    public static void measurePerformance(Runnable runnable, String operationDescription) {
        long startTime = System.currentTimeMillis();
        runnable.run();
        long endTime = System.currentTimeMillis();

        double elapsedTime = (endTime - startTime) / 1000.0;
        log.info(operationDescription + " 소요 시간: " + elapsedTime + "초");
    }
}


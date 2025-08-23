package com.solit.sync2sing.global.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

@Slf4j
public class MeasureTime {

    public static <T> T run(String name, Supplier<T> supplier) {
        long startNs = System.nanoTime();
        try {
            return supplier.get();
        } finally {
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
            log.info("[실행시간] {}: {} ms", name, elapsedMs);
        }
    }

    public static void run(String name, Runnable runnable) {
        long startNs = System.nanoTime();
        try {
            runnable.run();
        } finally {
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
            log.info("[실행시간] {}: {} ms", name, elapsedMs);
        }
    }

    public static <T> T call(String name, Callable<T> callable) throws Exception {
        long startNs = System.nanoTime();
        try {
            return callable.call();
        } finally {
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
            log.info("[실행시간] {}: {} ms", name, elapsedMs);
        }
    }
}

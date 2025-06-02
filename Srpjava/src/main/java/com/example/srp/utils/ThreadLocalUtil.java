package com.example.srp.utils;

public class ThreadLocalUtil {
    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove(); // 防止内存泄漏
    }
}

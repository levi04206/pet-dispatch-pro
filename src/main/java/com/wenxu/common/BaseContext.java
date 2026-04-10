package com.wenxu.common;

/**
 * 基于 ThreadLocal 封装的工具类，用于保存和获取当前登录用户 ID
 */
public class BaseContext {

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

    public static void removeCurrentId() {
        threadLocal.remove(); // 🚨 极客提示：用完一定要删，防止内存泄漏！
    }
}
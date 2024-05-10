/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.alipay.sofa.koupleless.common.api;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: BizRunnable.java, v 0.1 2024年05月10日 11:28 立蓬 Exp $
 */
public class KouplelessRunnable implements Runnable {
    private Runnable runnable;

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public KouplelessRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public static Runnable wrap(Runnable runnable) {
        return new KouplelessRunnable(runnable);
    }

    @Override
    public void run() {
        // 使用创建时的 classloader
        ClassLoader currentClassloader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            runnable.run();
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassloader);
        }
    }
}
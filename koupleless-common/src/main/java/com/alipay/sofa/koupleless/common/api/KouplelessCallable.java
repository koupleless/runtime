package com.alipay.sofa.koupleless.common.api;

import java.util.concurrent.Callable;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: BizCallable.java, v 0.1 2024年05月10日 11:31 立蓬 Exp $
 */
public class KouplelessCallable<T> implements Callable<T> {
    private Callable<T> callable;

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public KouplelessCallable(Callable<T> callable) {
        this.callable = callable;
    }

    public static <T> Callable<T> wrap(Callable<T> callable) {
        return new KouplelessCallable<T>(callable);
    }

    @Override
    public T call() throws Exception {
        ClassLoader currentClassloader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return this.callable.call();
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassloader);
        }
    }
}
package com.alipay.sofa.koupleless.common.util;

import com.alipay.sofa.koupleless.common.api.KouplelessCallable;
import com.alipay.sofa.koupleless.common.api.KouplelessRunnable;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: KouplelessThreadPoolExecutor.java, v 0.1 2024年05月10日 11:29 立蓬 Exp $
 */
public class KouplelessThreadPoolExecutor extends ThreadPoolExecutor {
    public KouplelessThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                        BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public KouplelessThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                        BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public KouplelessThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                        BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public KouplelessThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                        BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }


    public void execute(Runnable runnable) {
        super.execute(getRunnable(runnable));
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        return super.submit(getRunnable(runnable));
    }

    @Override
    public <T> Future<T> submit(Runnable runnable, T result) {
        return super.submit(getRunnable(runnable), result);
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        return super.submit(getCallable(callable));
    }

    private Runnable getRunnable(Runnable runnable) {
        if(runnable instanceof KouplelessRunnable) {
            return runnable;
        }
        return wrapRunnable(runnable);
    }

    private <T> Callable<T> getCallable(Callable<T> callable) {
        if(callable instanceof KouplelessCallable) {
            return callable;
        }
        return KouplelessCallable.wrap(callable);
    }

    private Runnable wrapRunnable(Runnable runnable) {
        return KouplelessRunnable.wrap(runnable);
    }

    private <T> Callable<T> wrapCallable(Callable<T> callable) {
        return KouplelessCallable.wrap(callable);
    }
}
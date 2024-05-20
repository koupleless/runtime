/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.koupleless.plugin.concurrent;

import com.alipay.sofa.koupleless.plugin.manager.handler.ShutdownExecutorServicesOnUninstallEventHandler;

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
    public KouplelessThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                        TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        ShutdownExecutorServicesOnUninstallEventHandler.manageExecutorService(this);
    }

    public KouplelessThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                        TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                        ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        ShutdownExecutorServicesOnUninstallEventHandler.manageExecutorService(this);
    }

    public KouplelessThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                        TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                        RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
        ShutdownExecutorServicesOnUninstallEventHandler.manageExecutorService(this);
    }

    public KouplelessThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                        TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                        ThreadFactory threadFactory,
                                        RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory,
            handler);
        ShutdownExecutorServicesOnUninstallEventHandler.manageExecutorService(this);
    }

    public void execute(Runnable runnable) {
        super.execute(KouplelessRunnable.wrap(runnable));
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        return super.submit(KouplelessRunnable.wrap(runnable));
    }

    @Override
    public <T> Future<T> submit(Runnable runnable, T result) {
        return super.submit(KouplelessRunnable.wrap(runnable), result);
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        return super.submit(KouplelessCallable.wrap(callable));
    }
}
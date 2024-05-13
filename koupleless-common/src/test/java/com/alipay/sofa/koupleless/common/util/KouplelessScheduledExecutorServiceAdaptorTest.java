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
package com.alipay.sofa.koupleless.common.util;

import com.alipay.sofa.ark.common.util.ClassLoaderUtils;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Test;

import java.net.URLClassLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: KouplelessScheduledExecutorServiceAdaptorTest.java, v 0.1 2024年05月14日 00:05 立蓬 Exp $
 */
public class KouplelessScheduledExecutorServiceAdaptorTest {

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

    private ScheduledExecutorService executor                 = new KouplelessScheduledExecutorServiceAdaptor(
        scheduledExecutorService);

    private URLClassLoader           classLoader              = mockClassLoader();

    @Test
    public void test() {
        ClassLoader currentCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            Runnable runnable = () -> assertEquals(classLoader,
                Thread.currentThread().getContextClassLoader());
            Callable<String> callable = () -> {
                assertEquals(classLoader, Thread.currentThread().getContextClassLoader());
                return "mock";
            };
            executor.submit(runnable);
            executor.submit(callable);
            executor.submit(runnable, "mock");
            executor.invokeAll(Lists.newArrayList(callable));
            executor.invokeAll(Lists.newArrayList(callable), 2, TimeUnit.SECONDS);
            executor.invokeAny(Lists.newArrayList(callable));
            executor.invokeAny(Lists.newArrayList(callable), 2, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(currentCl);
        }
    }

    private URLClassLoader mockClassLoader() {
        return new URLClassLoader(ClassLoaderUtils.getURLs(this.getClass().getClassLoader()));
    }

    @After
    public void after() {
        executor.shutdown();
    }
}
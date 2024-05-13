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
import org.junit.After;
import org.junit.Test;

import java.net.URLClassLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: KouplelessThreadPoolExecutorTest.java, v 0.1 2024年05月10日 12:04 立蓬 Exp $
 */
public class KouplelessThreadPoolExecutorTest {

    private URLClassLoader classLoader = mockClassLoader();
    private KouplelessThreadPoolExecutor executor = new KouplelessThreadPoolExecutor(10, 10, 60L,
            TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    @Test
    public void test() {
        ClassLoader currentCl = Thread.currentThread().getContextClassLoader();
        try {

            Runnable runnable = () -> assertEquals(classLoader, Thread.currentThread().getContextClassLoader());

            Callable<String> callable = () -> {
                assertEquals(classLoader, Thread.currentThread().getContextClassLoader());
                return "mock";
            };

            Thread.currentThread().setContextClassLoader(classLoader);
            executor.submit(runnable);
            executor.submit(callable);
            executor.submit(runnable,"mock");

        } finally {
            Thread.currentThread().setContextClassLoader(currentCl);
        }
    }

    private URLClassLoader mockClassLoader() {
        return new URLClassLoader(ClassLoaderUtils.getURLs(this.getClass().getClassLoader()));
    }

    @After
    public void after(){
        executor.shutdown();
    }
}
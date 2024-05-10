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
package com.alipay.sofa.koupleless.common;

import com.alipay.sofa.ark.common.util.ClassLoaderUtils;
import com.alipay.sofa.koupleless.common.api.KouplelessRunnable;
import com.alipay.sofa.koupleless.common.util.KouplelessThreadPoolExecutor;
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
    @Test
    public void testSubmit() {
        KouplelessThreadPoolExecutor executor = new KouplelessThreadPoolExecutor(1, 1, 60L,
            TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
        URLClassLoader mockClassLoader = mockClassLoader();
        Thread.currentThread().setContextClassLoader(mockClassLoader);
        executor.submit(() -> {
            assertEquals(mockClassLoader, Thread.currentThread().getContextClassLoader());
        });

        executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                assertEquals(mockClassLoader, Thread.currentThread().getContextClassLoader());
                return "mock";
            }
        });

        executor.shutdown();
    }

    private URLClassLoader mockClassLoader() {
        return new URLClassLoader(ClassLoaderUtils.getURLs(this.getClass().getClassLoader()));
    }
}
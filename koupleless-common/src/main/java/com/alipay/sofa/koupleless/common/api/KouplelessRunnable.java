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
package com.alipay.sofa.koupleless.common.api;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: BizRunnable.java, v 0.1 2024年05月10日 11:28 立蓬 Exp $
 */
public class KouplelessRunnable implements Runnable {
    private Runnable    runnable;

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public KouplelessRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public static Runnable wrap(Runnable runnable) {
        if (runnable == null)
            throw new NullPointerException();

        if (runnable instanceof KouplelessRunnable) {
            return runnable;
        }
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
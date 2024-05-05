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
package com.alipay.sofa.koupleless.arklet.core.command.executor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>NamedThreadFactory class.</p>
 *
 * @author yuanyuan
 * @author zzl_i
 * @since 2023/9/1 12:02 下午
 * @version 1.0.0
 */
public class NamedThreadFactory implements ThreadFactory {

    private static final AtomicInteger POOL_COUNT   = new AtomicInteger();

    /**
     * The current thread pool counter
     */
    private final AtomicInteger        threadCount  = new AtomicInteger(1);

    /**
     * Thread group
     */
    private final ThreadGroup          group;

    /**
     * Thread name prefix
     */
    private final String               namePrefix;

    /**
     * Thread daemon option
     */
    private final boolean              isDaemon;

    /**
     * The first default prefix of thread name
     */
    private final static String        FIRST_PREFIX = "ARKLET-";

    /**
     * specify the second prefix of thread name, default the thread created is non-daemon
     *
     * @param secondPrefix second prefix of thread name
     */
    public NamedThreadFactory(String secondPrefix) {
        this(secondPrefix, false);
    }

    /**
     * Construct a named thread factory
     *
     * @param secondPrefix second prefix of thread name
     * @param daemon thread daemon option
     */
    public NamedThreadFactory(String secondPrefix, boolean daemon) {
        SecurityManager sm = System.getSecurityManager();
        group = (sm != null) ? sm.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = FIRST_PREFIX + secondPrefix + "-" + POOL_COUNT.getAndIncrement() + "-T";
        isDaemon = daemon;
    }

    /** {@inheritDoc} */
    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadCount.getAndIncrement(), 0);
        t.setDaemon(isDaemon);
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}

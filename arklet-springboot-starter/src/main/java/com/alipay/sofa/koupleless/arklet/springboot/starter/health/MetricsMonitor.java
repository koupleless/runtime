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
package com.alipay.sofa.koupleless.arklet.springboot.starter.health;

import com.alipay.sofa.koupleless.arklet.core.health.model.ClientMetrics;
import com.alipay.sofa.koupleless.arklet.core.util.DateUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: MetricsMonitor.java, v 0.1 2024年07月02日 13:55 立蓬 Exp $
 */
public class MetricsMonitor {

    public static ClientMetrics captureMetrics() {
        return ClientMetrics.builder().timestamp(DateUtils.getCurrentTime())
            .clientMetaspaceMetrics(reportMetaspace()).build();
    }

    private static ClientMetrics.ClientMemoryMetrics reportMetaspace() {
        for (MemoryPoolMXBean memoryMXBean : ManagementFactory.getMemoryPoolMXBeans()) {
            if ("Metaspace".equals(memoryMXBean.getName())) {
                MemoryUsage usage = memoryMXBean.getUsage();
                return new ClientMetrics.ClientMemoryMetrics(usage.getInit(), usage.getUsed(),
                    usage.getCommitted(), usage.getMax());
            }
        }
        return null;
    }

    public static boolean validateMetaspace(long metaspaceThreshold, ClientMetrics metrics) {
        if (metrics == null) {
            return true;
        }
        ClientMetrics.ClientMemoryMetrics metaspaceReport = metrics.getClientMetaspaceMetrics();
        long committed = metaspaceReport.getCommitted();
        long max = metaspaceReport.getMax();
        return (max == -1) || ((double) committed / max) * 100 <= metaspaceThreshold;
    }
}
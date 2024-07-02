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
package com.alipay.sofa.koupleless.arklet.core.monitor;

import com.alipay.sofa.koupleless.arklet.core.monitor.model.ClientMetrics;
import com.alipay.sofa.koupleless.arklet.core.monitor.model.GarbageCollectorName;
import com.alipay.sofa.koupleless.arklet.core.util.DateUtils;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;

import static com.alipay.sofa.koupleless.arklet.core.health.model.Constants.BYTE_TO_MB;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: MetricsMonitor.java, v 0.1 2024年07月02日 13:55 立蓬 Exp $
 */
public class MetricsMonitor {

    public static ClientMetrics captureMetrics() {
        return ClientMetrics.builder().timestamp(DateUtils.getCurrentTime())
            .metaspaceMetrics(reportMetaspace()).GCMetrics(reportGC())
            .metaspaceMetrics(reportMetaspace()).noHeapMetrics(reportNoheap())
            .heapMetrics(reportHeap()).systemMetrics(reportSystem()).threadMetrics(reportThread())
            .classLoadingMetrics(reportClassLoading()).build();
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

    private static ClientMetrics.ClientMemoryMetrics reportHeap() {
        MemoryUsage usage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        return new ClientMetrics.ClientMemoryMetrics(usage.getInit(), usage.getUsed(),
            usage.getCommitted(), usage.getMax());
    }

    private static ClientMetrics.ClientMemoryMetrics reportNoheap() {
        MemoryUsage usage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
        return new ClientMetrics.ClientMemoryMetrics(usage.getInit(), usage.getUsed(),
            usage.getCommitted(), usage.getMax());
    }

    private static ClientMetrics.ClientSystemMetrics reportSystem() {
        OperatingSystemMXBean origin = ManagementFactory.getOperatingSystemMXBean();
        double systemLoadAverage = origin.getSystemLoadAverage();
        double processCpuLoad = 0;
        double systemCpuLoad = 0;
        long freePhysicalMemorySize = 0L;
        long totalPhysicalMemorySize = 0L;
        if (origin instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) origin;
            processCpuLoad = operatingSystemMXBean.getProcessCpuLoad();
            systemCpuLoad = operatingSystemMXBean.getSystemCpuLoad();
            freePhysicalMemorySize = operatingSystemMXBean.getFreePhysicalMemorySize() / BYTE_TO_MB;
            totalPhysicalMemorySize = operatingSystemMXBean.getTotalPhysicalMemorySize()
                                      / BYTE_TO_MB;
        }
        return new ClientMetrics.ClientSystemMetrics(systemLoadAverage, processCpuLoad,
            systemCpuLoad, freePhysicalMemorySize, totalPhysicalMemorySize);
    }

    private static ClientMetrics.ClientGCMetrics reportGC() {
        long FGCCount = 0, FGCTime = 0, YGCCount = 0, YGCTime = 0;
        List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gc : gcs) {
            if (GarbageCollectorName.belongToFgc(gc.getName())) {
                FGCCount += gc.getCollectionCount();
                FGCTime += gc.getCollectionTime();
            }
            if (GarbageCollectorName.belongToYgc(gc.getName())) {
                YGCCount += gc.getCollectionCount();
                YGCTime += gc.getCollectionTime();
            }
        }
        return new ClientMetrics.ClientGCMetrics(FGCCount, FGCTime, YGCCount, YGCTime);
    }

    private static ClientMetrics.ClientThreadMetrics reportThread() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        int threadCount = threadMXBean.getThreadCount();
        int daemonThreadCount = threadMXBean.getDaemonThreadCount();
        int peakThreadCount = threadMXBean.getPeakThreadCount();
        long totalStartedThreadCount = threadMXBean.getTotalStartedThreadCount();
        return new ClientMetrics.ClientThreadMetrics(threadCount, daemonThreadCount,
            peakThreadCount, totalStartedThreadCount);
    }

    private static ClientMetrics.ClientClassLoadingMetrics reportClassLoading() {
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        long totalLoadedClassCount = classLoadingMXBean.getTotalLoadedClassCount();
        int loadedClassCount = classLoadingMXBean.getLoadedClassCount();
        long unloadedClassCount = classLoadingMXBean.getUnloadedClassCount();
        return new ClientMetrics.ClientClassLoadingMetrics(totalLoadedClassCount, loadedClassCount,
            unloadedClassCount);
    }

    public static boolean validateMetaspace(long metaspaceThreshold, ClientMetrics metrics) {
        if (metrics == null) {
            return true;
        }
        ClientMetrics.ClientMemoryMetrics metaspaceReport = metrics.getMetaspaceMetrics();
        long committed = metaspaceReport.getCommitted();
        long max = metaspaceReport.getMax();
        return (max == -1) || ((double) committed / max) * 100 <= metaspaceThreshold;
    }
}
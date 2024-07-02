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
package com.alipay.sofa.koupleless.arklet.core.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ClientMetrics.java, v 0.1 2024年07月02日 13:32 立蓬 Exp $
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClientMetrics {

    private String                    timestamp;

    private ClientMemoryMetrics       metaspaceMetrics;

    private ClientMemoryMetrics       noHeapMetrics;

    private ClientMemoryMetrics       heapMetrics;

    private ClientGCMetrics           GCMetrics;

    private ClientSystemMetrics       systemMetrics;

    private ClientThreadMetrics       threadMetrics;

    private ClientClassLoadingMetrics classLoadingMetrics;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClientMemoryMetrics {
        private long init;
        private long used;
        private long committed;
        private long max;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClientSystemMetrics {
        /**
         * the system load average for the last minute
         * see：https://kb.novaordis.com/index.php/OperatingSystemMXBean_Platform_MBean
         */
        private Double systemLoadAverage;

        /**
         * the CPU usage of the current JVM process returned as a double between 0.0 and 1.0 so simply multiply by 100 to get a percentage.
         * see：https:/ /kb.novaordis.com/index.php/OperatingSystemMXBean_Platform_MBean
         */
        private Double processCpuLoad;

        /**
         * the CPU usage of the whole system returned as a double between 0.0
         * and 1.0 so simply multiply by 100 to get a percentage.
         * see：https://kb.novaordis.com/index.php/OperatingSystemMXBean_Platform_MBean
         */
        private Double systemCpuLoad;

        /**
         * 剩余物理内存空间，单位：MB
         */
        private Long   freePhysicalMemorySize;

        /**
         * 总物理内存空间，单位：MB
         */
        private Long   totalPhysicalMemorySize;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClientGCMetrics {

        /**
         * full GC 次数
         */
        private Long fgcCount;

        /**
         * full GC 时间，单位：milliseconds
         */
        private Long fgcTime;

        /**
         * young GC 次数
         */
        private Long ygcCount;

        /**
         * young GC 时间，单位：milliseconds
         */
        private Long ygcTime;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClientClassLoadingMetrics {
        private Long    totalLoadedClassCount;

        private Integer loadedClassCount;

        private Long    unloadedClassCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClientThreadMetrics {

        private Integer threadCount;

        private Integer daemonThreadCount;

        private Integer peakThreadCount;

        private Long    totalStartedThreadCount;
    }
}
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
package com.alipay.sofa.koupleless.arklet.core.health.indicator;

import com.alipay.sofa.koupleless.arklet.core.health.model.Constants;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

/**
 * <p>CpuIndicator class.</p>
 *
 * @author Lunarscave
 * @version 1.0.0
 */
public class CpuIndicator extends Indicator {

    private final CpuIndicatorHandler cpuIndicatorHandler;

    private static final String       CPU_INDICATOR_ID = Constants.CPU;

    /**
     * <p>Constructor for CpuIndicator.</p>
     */
    public CpuIndicator() {
        super(CPU_INDICATOR_ID);
        cpuIndicatorHandler = new CpuIndicatorHandler();
    }

    /** {@inheritDoc} */
    @Override
    protected Map<String, Object> getHealthDetails() {
        Map<String, Object> cpuHealthDetails = new HashMap<>(6);

        cpuIndicatorHandler.collectTicks();
        cpuHealthDetails.put(CpuMetrics.CPU_COUNT.getId(), cpuIndicatorHandler.getCpuCount());
        cpuHealthDetails.put(CpuMetrics.CPU_TYPE.getId(), cpuIndicatorHandler.getCpuType());
        cpuHealthDetails.put(CpuMetrics.CPU_TOTAL_USED.getId(), cpuIndicatorHandler.getTotalUsed());
        cpuHealthDetails.put(CpuMetrics.CPU_USER_USED.getId(), cpuIndicatorHandler.getUserUsed());
        cpuHealthDetails.put(CpuMetrics.CPU_SYSTEM_USED.getId(),
            cpuIndicatorHandler.getSystemUsed());
        cpuHealthDetails.put(CpuMetrics.CPU_FREE.getId(), cpuIndicatorHandler.getFree());
        return cpuHealthDetails;
    }

    static class CpuIndicatorHandler {

        private final CentralProcessor cpu;

        private long[]                 prevTicks;

        private long[]                 nextTicks;

        private String                 cpuType;

        public CpuIndicatorHandler() {
            try {
                this.cpu = new SystemInfo().getHardware().getProcessor();
                prevTicks = cpu.getSystemCpuLoadTicks();
                try {
                    Method method = cpu.getClass().getMethod("getName");
                    this.cpuType = (String) method.invoke(cpu, null);
                } catch (NoSuchMethodException | IllegalAccessException
                        | InvocationTargetException e) {
                    this.cpuType = cpu.getProcessorIdentifier().getName();
                }
            } catch (Throwable t) {
                throw new RuntimeException(
                    "\n【Reason】This Exception happened when oshi-core defined in koupleless is not supported in project."
                                           + "\n【Solution】Please define the version of oshi-core which supported in your pom dependencyManagement!",
                    t);
            }
        }

        public void collectTicks() {
            nextTicks = cpu.getSystemCpuLoadTicks();
        }

        public double getTotalUsed() {
            Set<CentralProcessor.TickType> tickTypeSet = new HashSet<CentralProcessor.TickType>(
                Arrays.asList(CentralProcessor.TickType.class.getEnumConstants()));
            double totalUsed = 0;
            for (CentralProcessor.TickType tickType : tickTypeSet) {
                totalUsed += nextTicks[tickType.getIndex()] - prevTicks[tickType.getIndex()];
            }
            return totalUsed;
        }

        public double getUserUsed() {
            return getUsed(CentralProcessor.TickType.USER);
        }

        public double getSystemUsed() {
            return getUsed(CentralProcessor.TickType.SYSTEM);
        }

        public double getFree() {
            return getUsed(CentralProcessor.TickType.IDLE);
        }

        public int getCpuCount() {
            return cpu.getLogicalProcessorCount();
        }

        public String getCpuType() {
            return this.cpuType;
        }

        private double getUsed(CentralProcessor.TickType tickType) {
            double totalUsed = getTotalUsed();
            double used = nextTicks[tickType.getIndex()] - prevTicks[tickType.getIndex()];
            if (totalUsed == 0 || used < 0) {
                used = 0;
            } else {
                used = 100d * used / totalUsed;
            }
            return used;
        }
    }

    enum CpuMetrics {

                     CPU_COUNT("count"), CPU_TYPE("type"), CPU_TOTAL_USED("total used (%)"), CPU_USER_USED("user used (%)"), CPU_SYSTEM_USED("system used (%)"), CPU_FREE("free (%)");

        private final String id;

        CpuMetrics(String desc) {
            this.id = desc;
        }

        public String getId() {
            return id;
        };
    }
}

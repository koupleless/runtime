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

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: GarbageCollectorName.java, v 0.1 2024年07月02日 15:41 立蓬 Exp $
 */
public enum GarbageCollectorName {

                                  MarkSweepCompact("MarkSweepCompact"), ParallelOld("ParallelOld"), SerialOld("SerialOld"), PSMarkSweep("PSMarkSweep"), PS_MarkSweep("PS MarkSweep"), Copy("Copy"), PSScavenge("PSScavenge"), PS_Scavenge("PS Scavenge"), ParallelScavenge("ParallelScavenge"), DefNew("DefNew"), ParNew("ParNew"), G1New("G1New"), ConcurrentMarkSweep("ConcurrentMarkSweep"), G1Old("G1Old"), GCNameEndSentinel("GCNameEndSentinel"),

                                  G1YoungGeneration("G1 Young Generation"), G1OldGeneration("G1 Old Generation");

    public static List<String> fgcList = asList(G1OldGeneration.name(), ConcurrentMarkSweep.name(),
        ParallelOld.name(), SerialOld.name(), G1Old.name());

    public static List<String> ygcList = asList(G1YoungGeneration.name(), ParNew.name(),
        G1New.name(), ParallelScavenge.name());

    @Getter
    @Setter
    private String             value;

    GarbageCollectorName(String val) {
        this.value = val;
    }

    public static boolean belongToFgc(String gcName) {
        return fgcList.contains(gcName);
    }

    public static boolean belongToYgc(String gcName) {
        return ygcList.contains(gcName);
    }
}
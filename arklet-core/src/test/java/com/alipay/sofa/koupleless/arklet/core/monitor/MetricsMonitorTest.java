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
import com.alipay.sofa.koupleless.arklet.core.monitor.model.ClientMetrics.ClientMemoryMetrics;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: MetricsMonitorTest.java, v 0.1 2024年07月02日 14:47 立蓬 Exp $
 */
public class MetricsMonitorTest {
    @Test
    public void testCaptureMetrics() {
        ClientMetrics metrics = ClientMetrics.builder()
            .metaspaceMetrics(
                ClientMemoryMetrics.builder().max(100000).committed(85000).used(80000).build())
            .build();

        assertTrue(MetricsMonitor.validateMetaspace(90, metrics));
        assertFalse(MetricsMonitor.validateMetaspace(80, metrics));
    }
}
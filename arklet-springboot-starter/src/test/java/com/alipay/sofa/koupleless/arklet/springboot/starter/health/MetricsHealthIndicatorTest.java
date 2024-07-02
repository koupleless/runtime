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
import com.alipay.sofa.koupleless.arklet.springboot.starter.properties.ArkletProperties.MonitorProperties;
import org.junit.Test;
import org.springframework.boot.actuate.health.Status;

import static org.junit.Assert.assertEquals;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: MetricsHealthIndicatorTest.java, v 0.1 2024年07月02日 14:32 立蓬 Exp $
 */
public class MetricsHealthIndicatorTest {

    @Test
    public void testMetaspace() {
        MonitorProperties monitorProperties = new MonitorProperties();

        MetricsHealthIndicator indicator = new MetricsHealthIndicator(monitorProperties);
        assertEquals(Status.UP, indicator.health().getStatus());

        monitorProperties.setMetaspaceThreshold(0);
        assertEquals(Status.DOWN, indicator.health().getStatus());
    }

}
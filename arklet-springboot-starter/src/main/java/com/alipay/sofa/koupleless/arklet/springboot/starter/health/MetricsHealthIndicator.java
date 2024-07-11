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

import com.alipay.sofa.koupleless.arklet.core.monitor.MetricsMonitor;
import com.alipay.sofa.koupleless.arklet.core.monitor.model.ClientMetrics;
import com.alipay.sofa.koupleless.arklet.springboot.starter.properties.ArkletProperties.MonitorProperties;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;

import static com.alipay.sofa.koupleless.arklet.core.monitor.MetricsMonitor.validateMetaspace;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: MetaspaceHealthIndicator.java, v 0.1 2024年07月02日 11:50 立蓬 Exp $
 */
public class MetricsHealthIndicator extends AbstractHealthIndicator {
    private final MonitorProperties monitorProperties;

    public MetricsHealthIndicator(MonitorProperties monitorProperties) {
        this.monitorProperties = monitorProperties;
    }

    @Override
    protected void doHealthCheck(Builder builder) throws Exception {
        ClientMetrics clientMetrics = MetricsMonitor.captureMetrics();
        builder.withDetail("metrics", clientMetrics);

        if (unhealthy(clientMetrics)) {
            builder.down();
        } else {
            builder.up();
        }
    }

    private boolean unhealthy(ClientMetrics clientMetrics) {
        // metaspace is checked
        return monitorProperties.isMetaspaceCheck()
               && !validateMetaspace(monitorProperties.getMetaspaceThreshold(), clientMetrics);
    }
}
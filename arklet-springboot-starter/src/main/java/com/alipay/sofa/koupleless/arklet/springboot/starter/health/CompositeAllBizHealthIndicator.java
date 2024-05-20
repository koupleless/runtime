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

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.koupleless.common.BizRuntimeContext;
import com.alipay.sofa.koupleless.common.BizRuntimeContextRegistry;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 整合所有 Biz 的健康指标
 *
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: CompositeAllBizHealthIndicator.java, v 1.1.0 2024年03月21日 11:56 立蓬 Exp $
 * @since 1.1.0
 */
public class CompositeAllBizHealthIndicator extends AbstractHealthIndicator {

    /**
     * this is ugly, but we need to support both springboot1.x, 2.x and above, we need to use reflection to support both
     */
    public Method healthBuildWithDetails = null;

    public CompositeAllBizHealthIndicator() {
        try {
            healthBuildWithDetails = Health.Builder.class.getDeclaredMethod("withDetails",
                Map.class);
        } catch (NoSuchMethodException e) {
            // ignore
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void doHealthCheck(Builder builder) throws Exception {
        Map<String, Health> bizHealthMap = aggregateBizHealth();

        builder.up();
        if (healthBuildWithDetails != null) {
            healthBuildWithDetails.invoke(builder, bizHealthMap);
        }

        if (bizHealthMap.values().stream().map(Health::getStatus).anyMatch(Status.DOWN::equals)) {
            builder.down();
        } else if (bizHealthMap.values().stream().map(Health::getStatus)
            .anyMatch(Status.UNKNOWN::equals)) {
            builder.unknown();
        }
    }

    private HashMap<String, Health> aggregateBizHealth() {
        HashMap<String, Health> bizHealthMap = new HashMap<>();

        ConcurrentHashMap<ClassLoader, BizRuntimeContext> runtimeMap = BizRuntimeContextRegistry
            .getRuntimeMap();
        runtimeMap.forEach((classLoader, bizRuntimeContext) -> {
            if (classLoader == ArkClient.getMasterBiz().getBizClassLoader()) {
                return; // only skips this iteration.
            }

            Biz biz = ArkClient.getBizManagerService().getBizByClassLoader(classLoader);
            Map<String, HealthIndicator> bizIndicators = bizRuntimeContext
                .getRootApplicationContext().getBeansOfType(HealthIndicator.class);
            Health.Builder bizBuilder = new Health.Builder().up();

            bizIndicators.forEach((bizIndicatorBeanName, bizIndicator) -> {
                String bizIndicatorName = bizIndicatorBeanName.substring(0,
                    bizIndicatorBeanName.length() - "HealthIndicator".length());
                Health bizIndicatorHealth = bizIndicator.health();

                Map<String, Object> bizIndicatorDetails = new HashMap<>();
                bizIndicatorDetails.put("details", bizIndicatorHealth.getDetails());
                bizIndicatorDetails.put("status", bizIndicatorHealth.getStatus());

                bizBuilder.withDetail(bizIndicatorName, bizIndicatorDetails);
                if (bizIndicatorHealth.getStatus().equals(Status.DOWN)) {
                    bizBuilder.down();
                } else if (bizIndicatorHealth.getStatus().equals(Status.UNKNOWN)) {
                    bizBuilder.unknown();
                }
            });

            Health bizHealth = bizBuilder.build();
            bizHealthMap.put(biz.getIdentity(), bizHealth);
        });
        return bizHealthMap;
    }
}

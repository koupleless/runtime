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
import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.koupleless.arklet.springboot.starter.properties.ArkletProperties;
import com.alipay.sofa.koupleless.common.environment.ConditionalOnMasterBiz;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import static com.alipay.sofa.koupleless.arklet.springboot.starter.health.BaseStartUpHealthIndicator.WITH_ALL_BIZ_READINESS;

/**
 * <p>HealthAutoConfiguration class.</p>
 *
 * @author Lunarscave
 * @version 1.0.0
 */
@Configuration
@ConditionalOnMasterBiz
public class HealthAutoConfiguration {

    /**
     * <p>bizInfoContributor.</p>
     *
     * @return a {@link com.alipay.sofa.koupleless.arklet.springboot.starter.health.BizInfoContributor} object
     */
    @Bean
    @ConditionalOnClass(name = { "org.springframework.boot.actuate.info.InfoContributor" })
    public BizInfoContributor bizInfoContributor() {
        return new BizInfoContributor();
    }

    /**
     * <p>baseStartUpHealthIndicator.</p>
     *
     * @return a {@link com.alipay.sofa.koupleless.arklet.springboot.starter.health.BaseStartUpHealthIndicator} object
     */
    @Bean("baseStartUpHealthIndicator")
    @ConditionalOnClass(name = { "org.springframework.boot.actuate.health.AbstractHealthIndicator" })
    public BaseStartUpHealthIndicator baseStartUpHealthIndicator(Environment masterBizEnvironment) {
        BaseStartUpHealthIndicator indicator = new BaseStartUpHealthIndicator(Boolean
            .parseBoolean(masterBizEnvironment.getProperty(WITH_ALL_BIZ_READINESS, "false")));
        ArkClient.getEventAdminService().register(indicator);
        return indicator;
    }

    /**
     * <p>compositeAllBizHealthIndicator.</p>
     *
     * @return a {@link com.alipay.sofa.koupleless.arklet.springboot.starter.health.CompositeAllBizHealthIndicator} object
     */
    @Bean("compositeAllBizHealthIndicator")
    @ConditionalOnClass(name = { "org.springframework.boot.actuate.health.AbstractHealthIndicator" })
    public CompositeAllBizHealthIndicator compositeAllBizHealthIndicator() {
        return new CompositeAllBizHealthIndicator();
    }

    @Bean("metricsHealthIndicator")
    @ConditionalOnClass(name = { "org.springframework.boot.actuate.health.AbstractHealthIndicator" })
    public MetricsHealthIndicator metricsHealthIndicator(ArkletProperties arkletProperties) {
        return new MetricsHealthIndicator(arkletProperties.getMonitor());
    }
}

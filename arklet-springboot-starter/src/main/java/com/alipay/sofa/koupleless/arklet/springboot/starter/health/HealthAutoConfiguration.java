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
import com.alipay.sofa.koupleless.common.environment.ConditionalOnMasterBiz;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.alipay.sofa.koupleless.arklet.springboot.starter.health.BaseStartUpHealthIndicator.WITH_ALL_BIZ_READINESS;

/**
 * @author Lunarscave
 */
@Configuration
@ConditionalOnMasterBiz
public class HealthAutoConfiguration {

    @Bean
    public BizInfoContributor bizInfoContributor() {
        return new BizInfoContributor();
    }

    @Bean("baseStartUpHealthIndicator")
    public BaseStartUpHealthIndicator baseStartUpHealthIndicator() {
        BaseStartUpHealthIndicator indicator = new BaseStartUpHealthIndicator(
            Boolean.parseBoolean(EnvironmentUtils.getProperty(WITH_ALL_BIZ_READINESS, "false")));
        ArkClient.getEventAdminService().register(indicator);
        return indicator;
    }

    @Bean("compositeAllBizHealthIndicator")
    public CompositeAllBizHealthIndicator compositeAllBizHealthIndicator() {
        return new CompositeAllBizHealthIndicator();
    }
}

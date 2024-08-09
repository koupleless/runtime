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
import com.alipay.sofa.koupleless.arklet.springboot.starter.properties.ArkletProperties;
import com.alipay.sofa.koupleless.common.environment.ConditionalOnMasterBiz;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import static com.alipay.sofa.koupleless.arklet.springboot.starter.model.Constants.WITH_ALL_BIZ_READINESS;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ProbeAutoConfiguration.java, v 0.1 2024年07月04日 12:50 立蓬 Exp $
 */
@Configuration
@ConditionalOnMasterBiz
@ConditionalOnClass(name = "org.springframework.boot.availability.ApplicationAvailability")
@EnableConfigurationProperties(ArkletProperties.class)
public class ProbeAutoConfiguration {

    @Bean
    public ProbeAvailabilityStateIndicator probeAvailabilityStateIndicator(ApplicationContext applicationContext) {
        return new ProbeAvailabilityStateIndicator(applicationContext);
    }

    @Bean
    public BaseProbeAvailabilityStateHandler baseProbeAvailabilityStateHandler(ApplicationContext applicationContext,
                                                                               Environment env,
                                                                               ArkletProperties arkletProperties) {
        BaseProbeAvailabilityStateHandler handler = new BaseProbeAvailabilityStateHandler(
            applicationContext,
            Boolean.parseBoolean(env.getProperty(WITH_ALL_BIZ_READINESS, "false")),
            arkletProperties.getOperation().getSilenceSecondsBeforeUninstall());
        ArkClient.getEventAdminService().register(handler);
        return handler;
    }
}
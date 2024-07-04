/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.alipay.sofa.koupleless.arklet.springboot.starter.health;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.koupleless.common.environment.ConditionalOnMasterBiz;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.alipay.sofa.koupleless.arklet.springboot.starter.model.Constants.WITH_ALL_BIZ_READINESS;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ProbeAutoConfiguration.java, v 0.1 2024年07月04日 12:50 立蓬 Exp $
 */
@Configuration
@ConditionalOnMasterBiz
@ConditionalOnClass(name = "org.springframework.boot.availability.ApplicationAvailability")
public class ProbeAutoConfiguration {

    @Bean
    public ProbeAvailabilityStateIndicator probeAvailabilityStateIndicator(ApplicationContext applicationContext){
        return new ProbeAvailabilityStateIndicator(applicationContext);
    }


    @Bean
    public BaseProbeAvailabilityStateHandler baseProbeAvailabilityStateHandler(ApplicationContext applicationContext){
        BaseProbeAvailabilityStateHandler handler = new BaseProbeAvailabilityStateHandler(applicationContext,Boolean.parseBoolean(EnvironmentUtils.getProperty(WITH_ALL_BIZ_READINESS, "false")));
        ArkClient.getEventAdminService().register(handler);
        return handler;
    }
}
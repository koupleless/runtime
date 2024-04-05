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
package com.alipay.sofa.koupleless.plugin.spring;

import com.alipay.sofa.koupleless.common.BizRuntimeContext;
import com.alipay.sofa.koupleless.common.BizRuntimeContextRegistry;
import com.alipay.sofa.koupleless.common.util.ArkUtils;
import com.alipay.sofa.koupleless.common.util.PropertiesUtil;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;

import java.util.Set;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

@Order
public class SkipInitializerListener implements ApplicationListener<SpringApplicationEvent> {
    private static final Logger LOGGER                  = getLogger(SkipInitializerListener.class);

    public static final String MODULE_INITIALIZER_SKIP = "koupleless.module.initializer.skip";

    @Override
    public void onApplicationEvent(SpringApplicationEvent event) {
        try {
            if (ArkUtils.isModuleBiz()) {
                optimizeModule(event);
            }
        } catch (Exception e) {
            LOGGER.error("SkipInitializerListener run failed", e);
        }
    }

    protected void optimizeModule(SpringApplicationEvent event) {
        if (event instanceof ApplicationStartingEvent) {
            skipInitializers(event.getSpringApplication());
        }
    }

    protected void skipInitializers(SpringApplication application) {
        BizRuntimeContext bizRuntimeContext = BizRuntimeContextRegistry
            .getMasterBizRuntimeContext();
        Set<String> moduleInitializerSkips = PropertiesUtil.formatPropertyValues(bizRuntimeContext
            .getRootApplicationContext().getEnvironment().getProperty(MODULE_INITIALIZER_SKIP));

        application.setInitializers(application.getInitializers().stream()
            .filter(
                initializer -> !moduleInitializerSkips.contains(initializer.getClass().getName()))
            .collect(Collectors.toList()));
    }
}

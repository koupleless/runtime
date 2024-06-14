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
package com.alipay.sofa.koupleless.plugin;

import com.alipay.sofa.koupleless.common.BizRuntimeContext;
import com.alipay.sofa.koupleless.common.service.ArkAutowiredBeanPostProcessor;
import com.alipay.sofa.koupleless.common.BizRuntimeContextRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * <p>BaseRuntimeAutoConfiguration class.</p>
 *
 * @author mingmen
 * @since 2023/6/14
 * @version 1.0.0
 */
@Configuration
public class BaseRuntimeAutoConfiguration {

    /**
     * <p>bizRuntimeContext.</p>
     *
     * @param applicationContext a {@link org.springframework.context.ApplicationContext} object
     * @return a {@link com.alipay.sofa.koupleless.common.BizRuntimeContext} object
     */
    @Bean
    @ConditionalOnMissingClass("com.alipay.sofa.koupleless.test.suite.biz.TestBizClassLoader")
    public BizRuntimeContext bizRuntimeContext(ApplicationContext applicationContext) {
        ClassLoader classLoader = applicationContext.getClassLoader();
        BizRuntimeContext bizRuntimeContext = BizRuntimeContextRegistry
            .getBizRuntimeContextByClassLoader(classLoader);
        bizRuntimeContext.setRootApplicationContext(applicationContext);
        return bizRuntimeContext;
    }

    @Bean(name = "bizRuntimeContext")
    @ConditionalOnClass(name = "com.alipay.sofa.koupleless.test.suite.biz.TestBizClassLoader")
    public BizRuntimeContext bizRuntimeContextIntegrationTest(ApplicationContext applicationContext) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        BizRuntimeContext bizRuntimeContext = BizRuntimeContextRegistry
            .getBizRuntimeContextByClassLoader(classLoader);
        bizRuntimeContext.setRootApplicationContext(applicationContext);
        return bizRuntimeContext;
    }

    @EventListener
    public void onApplicationStartedEvent(ApplicationStartedEvent event) {
        // 获取 ApplicationContext
        ConfigurableApplicationContext applicationContext = event.getApplicationContext();
        ClassLoader classLoader = applicationContext.getClassLoader();
        BizRuntimeContext bizRuntimeContext = BizRuntimeContextRegistry
                .getBizRuntimeContextByClassLoader(classLoader);
        bizRuntimeContext.setRootApplicationContext(applicationContext);
    }

    /**
     * <p>arkAutowiredBeanPostProcessor.</p>
     *
     * @return a {@link com.alipay.sofa.koupleless.common.service.ArkAutowiredBeanPostProcessor} object
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = { "com.alipay.sofa.koupleless.common.service.ArkAutowiredBeanPostProcessor" })
    public ArkAutowiredBeanPostProcessor arkAutowiredBeanPostProcessor() {
        return new ArkAutowiredBeanPostProcessor();
    }
}

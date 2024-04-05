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

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.service.biz.BizManagerServiceImpl;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.koupleless.common.BizRuntimeContext;
import com.alipay.sofa.koupleless.common.BizRuntimeContextRegistry;
import com.alipay.sofa.koupleless.plugin.BaseRuntimeAutoConfiguration;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.net.URL;
import java.net.URLClassLoader;

import static com.alipay.sofa.koupleless.plugin.spring.SkipInitializerListener.MODULE_INITIALIZER_SKIP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SkipInitializerListenerTest {

    @Test
    public void onApplicationEvent() {
        // init master biz
        BizModel masterBiz = new BizModel();
        masterBiz.setBizName("masterBiz");
        masterBiz.setClassLoader(this.getClass().getClassLoader());
        ArkClient.setMasterBiz(masterBiz);

        BizRuntimeContext masterBizRuntimeContext = new BizRuntimeContext(masterBiz);
        masterBizRuntimeContext.setAppClassLoader(ClassLoader.getSystemClassLoader());
        ApplicationContext masterApplicationContext = mock(ApplicationContext.class);
        Environment environment = mock(ConfigurableEnvironment.class);
        when(environment.getProperty(MODULE_INITIALIZER_SKIP))
            .thenReturn(
                "org.springframework.boot.context.config.DelegatingApplicationContextInitializer,"
                        + "org.springframework.boot.autoconfigure.SharedMetadataReaderFactoryContextInitializer,"
                        + "org.springframework.boot.context.ContextIdApplicationContextInitializer,"
                        + "org.springframework.boot.context.ConfigurationWarningsApplicationContextInitializer");
        when(masterApplicationContext.getEnvironment()).thenReturn(environment);
        masterBizRuntimeContext.setRootApplicationContext(masterApplicationContext);
        BizRuntimeContextRegistry.registerBizRuntimeManager(masterBizRuntimeContext);

        BizManagerService bizManagerService = mock(BizManagerServiceImpl.class);
        when(bizManagerService.getBizByClassLoader(ClassLoader.getSystemClassLoader()))
            .thenReturn(masterBiz);

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // init biz
            Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[0]));

            BizModel otherBiz = new BizModel();
            otherBiz.setBizName("otherBiz");

            BizRuntimeContext otherBizRuntimeContext = new BizRuntimeContext(otherBiz);
            otherBizRuntimeContext
                .setAppClassLoader(Thread.currentThread().getContextClassLoader());
            when(bizManagerService
                .getBizByClassLoader(Thread.currentThread().getContextClassLoader()))
                    .thenReturn(otherBiz);
            ArkClient.setBizManagerService(bizManagerService);

            BizRuntimeContextRegistry.registerBizRuntimeManager(otherBizRuntimeContext);

            SpringApplicationBuilder builder = new SpringApplicationBuilder(TestConfiguration.class,
                BaseRuntimeAutoConfiguration.class);
            SpringApplication springApplication = builder.build();

            assertEquals(7, springApplication.getInitializers().size());

            assertThrows(IllegalArgumentException.class, () -> springApplication.run());

            assertEquals(3, springApplication.getInitializers().size());
            //            SpringApplication springApplication = new SpringApplication();
            //
            //            ApplicationContext applicationContext = springApplication.run();
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }

        int a = 1;
    }

    @Configuration
    static class TestConfiguration {
        @Bean
        public SkipInitializerListener skipInitializerListener() {
            return new SkipInitializerListener();
        }
    }
}

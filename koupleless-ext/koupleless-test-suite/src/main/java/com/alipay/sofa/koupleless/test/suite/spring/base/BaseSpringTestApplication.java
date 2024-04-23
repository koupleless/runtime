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
package com.alipay.sofa.koupleless.test.suite.spring.base;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.koupleless.common.BizRuntimeContext;
import com.alipay.sofa.koupleless.common.BizRuntimeContextRegistry;
import com.alipay.sofa.koupleless.test.suite.biz.TestBootstrap;
import com.alipay.sofa.koupleless.test.suite.spring.model.BaseSpringTestConfig;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;

import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

import static com.alipay.sofa.ark.spi.constant.Constants.MASTER_BIZ;

/**
 * @author CodeNoobKing
 * @date 2024/3/8
 */
@Getter
public class BaseSpringTestApplication {

    private BaseSpringTestConfig           config;

    private ConfigurableApplicationContext applicationContext;

    private URLClassLoader                 baseClassLoader;

    public void initBaseClassLoader() {
        URLClassLoader baseClassLoader = (URLClassLoader) Thread.currentThread()
            .getContextClassLoader();
        this.baseClassLoader = new BaseClassLoader(Lists.newArrayList(config.getArtifactId()),
            baseClassLoader);
    }

    public BaseSpringTestApplication(BaseSpringTestConfig config) {
        config.init();
        this.config = config;
    }

    private boolean isNotArkApplicationStartListener(ApplicationListener<?> listener) {
        return !listener.getClass().getName()
            .equals("com.alipay.sofa.ark.springboot.listener.ArkApplicationStartListener");
    }

    @SneakyThrows
    public void run() {
        Class<?> mainClass = baseClassLoader.loadClass(config.getMainClass().getName());
        // add necessary bizRuntimeContext
        SpringApplication springApplication = new SpringApplication(
            new DefaultResourceLoader(baseClassLoader), mainClass) {
            // the listener is not needed in the test workflow.
            // because it will automatically create another ArkServiceContainer with a unreachable Container ClassLoader
            @Override
            public void setListeners(Collection<? extends ApplicationListener<?>> listeners) {
                super.setListeners(listeners.stream()
                    .filter(BaseSpringTestApplication.this::isNotArkApplicationStartListener)
                    .collect(Collectors.toList()));
            }
        };
        springApplication.setAdditionalProfiles("base");

        springApplication
            .addListeners(new ApplicationListener<ApplicationEnvironmentPreparedEvent>() {
                @Override
                public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
                    TestBootstrap.registerMasterBiz();
                    ArkConfigs.setSystemProperty(MASTER_BIZ, ArkClient.getMasterBiz().getBizName());
                    BizRuntimeContext bizRuntimeContext = new BizRuntimeContext(
                        ArkClient.getMasterBiz());
                    BizRuntimeContextRegistry.registerBizRuntimeManager(bizRuntimeContext);
                }
            }, new ApplicationListener<ApplicationContextInitializedEvent>() {
                @Override
                public void onApplicationEvent(ApplicationContextInitializedEvent event) {
                    BizRuntimeContextRegistry
                            .getMasterBizRuntimeContext()
                        .setRootApplicationContext(event.getApplicationContext());
                }
            });

        applicationContext = springApplication.run();
    }
}

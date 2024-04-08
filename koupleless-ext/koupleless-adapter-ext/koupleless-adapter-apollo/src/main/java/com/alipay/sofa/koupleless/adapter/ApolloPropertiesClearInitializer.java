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
package com.alipay.sofa.koupleless.adapter;

import com.alipay.sofa.koupleless.common.util.ArkUtils;
import com.alipay.sofa.koupleless.common.util.MultiBizProperties;
import com.google.common.collect.Lists;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.StringUtils;

import java.util.List;

@ConditionalOnBean(type = "com.ctrip.framework.apollo.spring.boot.ApolloApplicationContextInitializer")
@ConditionalOnClass(name = "com.ctrip.framework.apollo.spring.boot.ApolloApplicationContextInitializer")
public class ApolloPropertiesClearInitializer implements EnvironmentPostProcessor, Ordered {

    private int                   order                 = -1;

    private static final String[] NEED_CLEAR_PROPERTIES = { "app.id", "apollo.cacheDir",
                                                            "apollo.accesskey.secret",
                                                            "apollo.property.order.enable" };

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {
        MultiBizProperties.initSystem();

        if(ArkUtils.isModuleBiz()){
            clearApolloSystemProperties(environment);
        }
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * 如果模块配置了 apollo 系统属性，则清理模块原本读取的基座 apollo 系统属性。该方法的效果是：
     * 1. 如果模块配置了 apollo 系统属性，如 app.id，则模块会读到模块配置的该属性。如：模块配置了 app.id=biz1，则模块会读到 app.id=biz1
     * 2. 如果模块没配置 apollo 系统属性，如 app.id，则模块的属性会使用基座的属性。如：模块没配置 app.id，基座配置了 app.id=base，则模块会读到 app.id=base
     */
    private void clearApolloSystemProperties(ConfigurableEnvironment environment) {
        List<String> propertiesToClear = apolloPropertiesConfiguredInBizEnvironment(environment);
        propertiesToClear.forEach(System::clearProperty);
    }

    private List<String> apolloPropertiesConfiguredInBizEnvironment(ConfigurableEnvironment environment) {
        List<String> properties = Lists.newArrayList();
        ConfigurableEnvironment tmpEnvironment = bizEnvironmentWithoutSystemProperties(environment);
        for (String key : NEED_CLEAR_PROPERTIES) {
            String value = tmpEnvironment.getProperty(key);
            if (!StringUtils.isEmpty(value)) {
                properties.add(key);
            }
        }
        return properties;
    }

    private ConfigurableEnvironment bizEnvironmentWithoutSystemProperties(ConfigurableEnvironment sourceEnvironment) {
        MutablePropertySources customPropertySources = new MutablePropertySources();
        sourceEnvironment.getPropertySources().stream().forEach(it -> {
            String name = it.getName();
            boolean notSystemProp = !StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME
                .equals(name);
            boolean notSystemEnv = !StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME
                .equals(name);
            if (notSystemProp && notSystemEnv) {
                customPropertySources.addLast(it);
            }
        });

        // to adaptor SpringBoot 2.1.9: new AbstractEnvironment instance and copy property sources
        ConfigurableEnvironment targetEnvironment = new AbstractEnvironment() {
        };
        MutablePropertySources envPropertySources = targetEnvironment.getPropertySources();
        customPropertySources.forEach(envPropertySources::addLast);
        return targetEnvironment;
    }
}

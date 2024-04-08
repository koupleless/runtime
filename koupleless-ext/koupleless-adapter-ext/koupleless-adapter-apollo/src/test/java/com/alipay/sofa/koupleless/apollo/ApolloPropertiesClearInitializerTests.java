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
package com.alipay.sofa.koupleless.apollo;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.service.classloader.BizClassLoader;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.koupleless.adapter.ApolloPropertiesClearInitializer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.mockStatic;

public class ApolloPropertiesClearInitializerTests {
    private ApolloPropertiesClearInitializer initializer   = new ApolloPropertiesClearInitializer();
    private ConfigurableEnvironment          environment;

    private Properties                       bizProperties = new Properties();

    private MockedStatic<ArkClient> arkClient;

    @Before
    public void before() {
        MutablePropertySources propertySources = new MutablePropertySources();
        propertySources.addLast(new SystemEnvironmentPropertySource(
            StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, (Map) System.getenv()));
        propertySources.addLast(new PropertiesPropertySource("bizProperties", bizProperties));
        environment = new AbstractEnvironment(propertySources) {
        };

        arkClient = mockStatic(ArkClient.class);
        Biz masterBiz = mockMasterBiz();
        arkClient.when(ArkClient::getMasterBiz).thenReturn(masterBiz);
    }

    @Test
    public void test() {
        BizClassLoader bizClassLoader = Mockito.mock(BizClassLoader.class);
        Thread.currentThread().setContextClassLoader(bizClassLoader);
        SpringApplication application = Mockito.mock(SpringApplication.class);

        String appIdKey = "app.id";
        String systemAppId = "system-app-id";
        System.setProperty(appIdKey, systemAppId);
        String bizAppId = "biz-app-id";
        bizProperties.setProperty(appIdKey, bizAppId);

        Assert.assertEquals(systemAppId, System.getProperty(appIdKey));
        initializer.postProcessEnvironment(environment, application);
        Assert.assertNull(System.getProperty(appIdKey));
    }

    private BizModel mockMasterBiz(){
        BizModel biz = new BizModel();
        biz.setClassLoader(Mockito.mock(BizClassLoader.class));
        return biz;
    }
}

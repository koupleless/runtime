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
import com.alipay.sofa.ark.container.service.biz.BizManagerServiceImpl;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.mock.env.MockEnvironment;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author yuanyuan
 * @since 2023/11/2 8:41 下午
 */
public class ServerlessEnvironmentPostProcessorTest {

    private final ConfigurableEnvironment masterEnvironment = mock(ConfigurableEnvironment.class);

    private final ConfigurableEnvironment otherEnvironment  = new MockEnvironment();

    private final SpringApplication       springApplication = mock(SpringApplication.class);

    private final Biz                     masterBiz         = mock(Biz.class);

    private final BizManagerService       bizManagerService = mock(BizManagerServiceImpl.class);

    private final Biz                     otherBiz          = mock(Biz.class);

    @Test
    public void testPostProcessEnvironment() {
        // process master biz
        when(masterEnvironment.getProperty("ark.common.env.share.keys")).thenReturn("masterKey");
        when(masterEnvironment.getProperty("masterKey")).thenReturn("masterValue");
        when(masterEnvironment.getProperty("logging.file.path")).thenReturn("./logs");
        MutablePropertySources masterPropertySources = new MutablePropertySources();
        masterPropertySources
            .addLast(new PropertiesPropertySource("masterProperties", new Properties()));
        when(masterEnvironment.getPropertySources()).thenReturn(masterPropertySources);

        ServerlessEnvironmentPostProcessor serverlessEnvironmentPostProcessor = new ServerlessEnvironmentPostProcessor();
        // reset ArkClient, to reinit the master environment.
        ArkClient.setMasterBiz(null);
        serverlessEnvironmentPostProcessor.postProcessEnvironment(masterEnvironment,
            springApplication);

        // process other biz
        ArkClient.setMasterBiz(masterBiz);
        when(masterBiz.getBizClassLoader()).thenReturn(ClassLoader.getSystemClassLoader());
        //        Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[0]));

        MutablePropertySources propertySources = otherEnvironment.getPropertySources();
        PropertiesPropertySource propertySource = new PropertiesPropertySource("xxxx111",
            new Properties());
        propertySources.addLast(propertySource);

        Properties properties = new Properties();
        properties.setProperty("kay", "kay_in_biz_application_properties");
        propertySources.addLast(
            new OriginTrackedMapPropertySource("mock application.properties in biz", properties));

        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        ArkClient.setBizManagerService(bizManagerService);
        doReturn(otherBiz).when(bizManagerService).getBizByClassLoader(any());
        doReturn("mockbiz").when(otherBiz).getBizName();

        // test with spring.config.location
        try {
            Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[0]));
            System.setProperty(ServerlessEnvironmentPostProcessor.SPRING_CONFIG_LOCATION, "xxxx");
            System.setProperty(ServerlessEnvironmentPostProcessor.SPRING_ACTIVE_PROFILES,
                "biz,abc");
            System.setProperty(ServerlessEnvironmentPostProcessor.SPRING_ADDITIONAL_LOCATION,
                "additional-location");
            serverlessEnvironmentPostProcessor.postProcessEnvironment(otherEnvironment,
                springApplication);
        } finally {
            System.clearProperty(ServerlessEnvironmentPostProcessor.SPRING_CONFIG_LOCATION);
            System.clearProperty(ServerlessEnvironmentPostProcessor.SPRING_ACTIVE_PROFILES);
            System.clearProperty(ServerlessEnvironmentPostProcessor.SPRING_ADDITIONAL_LOCATION);
            Thread.currentThread().setContextClassLoader(tccl);
        }

        PropertySource<?> masterPropertySource = propertySources.get("MasterBiz-Config resource");
        Assert.assertTrue(masterPropertySource instanceof MasterBizPropertySource);
        Assert.assertEquals("masterValue", masterPropertySource.getProperty("masterKey"));
        Assert.assertEquals("./logs",
            masterPropertySources.get("compatiblePropertySource").getProperty("logging.path"));

        // testBizApplicationPropertyInBizMultiDeployment
        Assert.assertEquals("abc", otherEnvironment.getProperty("kay"));
        PropertySource<?> otherPropertySource = propertySources
            .get("Biz-Config resourceconfig/mockbiz/application.properties");
        Assert.assertEquals("abc", otherPropertySource.getProperty("kay"));

        PropertySource<?> otherBizPropertySource = propertySources
            .get("Biz-Config resourceconfig/mockbiz/application-biz.properties");
        Assert.assertEquals("abc-biz", otherBizPropertySource.getProperty("kay1"));
    }
}

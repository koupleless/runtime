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
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.service.biz.BizManagerServiceImpl;
import com.alipay.sofa.ark.container.service.plugin.PluginManagerServiceImpl;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.model.Plugin;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.actuate.info.Info;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ArkInfoContributorTest.java, v 0.1 2024年03月22日 10:07 立蓬 Exp $
 */
public class ArkInfoContributorTest {
    @Test
    public void test(){
        try (MockedStatic<ArkClient> arkClient = mockStatic(ArkClient.class)) {

            BizManagerServiceImpl bizManagerService = new BizManagerServiceImpl();
            bizManagerService.registerBiz(mockMastertBiz());
            bizManagerService.registerBiz(mockBiz1());

            PluginManagerServiceImpl pluginManagerService = new PluginManagerServiceImpl();
            pluginManagerService.registerPlugin(mockPlugin());

            arkClient.when(ArkClient::getBizManagerService).thenReturn(bizManagerService);
            arkClient.when(ArkClient::getPluginManagerService).thenReturn(pluginManagerService);

            ArkInfoContributor contributor = new ArkInfoContributor();
            Info.Builder builder = new Info.Builder();
            contributor.contribute(builder);
            Map<String, Object> details = builder.build().getDetails();

            assertEquals(2, ((List)details.get("arkBizInfo")).size());
            assertEquals(1, ((List)details.get("arkPluginInfo")).size());
        }

    }

    private Biz mockMastertBiz() {
        ClassLoader masterBizClassLoader = mock(ClassLoader.class);
        BizModel masterBiz = new BizModel();
        masterBiz.setClassLoader(masterBizClassLoader);
        masterBiz.setBizName("masterBiz");
        masterBiz.setBizVersion("1.0.0");
        masterBiz.setBizState(BizState.RESOLVED);
        masterBiz.setPriority("0");
        return masterBiz;
    }

    private Biz mockBiz1() {
        ClassLoader bizClassLoader = mock(ClassLoader.class);
        BizModel biz = new BizModel();
        biz.setClassLoader(bizClassLoader);
        biz.setBizName("biz1");
        biz.setBizVersion("1.0.0");
        biz.setBizState(BizState.RESOLVED);
        biz.setPriority("100");
        return biz;
    }

    private Plugin mockPlugin() {
        PluginModel plugin = new PluginModel();
        plugin.setPluginName("plugin1");
        plugin.setVersion("1.0.0");
        plugin.setGroupId("com.mock");
        plugin.setArtifactId("plugin1");
        plugin.setPluginActivator("com.mock.Plugin1Activator");
        URL pluginUrl = mock(URL.class);
        doReturn("plugin1Path").when(pluginUrl).getPath();
        plugin.setPluginUrl(pluginUrl);
        plugin.setPriority("100");
        return plugin;
    }
}
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
import com.alipay.sofa.ark.container.service.biz.BizManagerServiceImpl;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.koupleless.common.BizRuntimeContext;
import com.alipay.sofa.koupleless.common.BizRuntimeContextRegistry;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ArkBizAggregateHealthIndicatorTest.java, v 0.1 2024年03月21日 15:04 立蓬 Exp $
 */
public class ArkBizAggregateHealthIndicatorTest {
    private ArkBizAggregateHealthIndicator indicator = new ArkBizAggregateHealthIndicator();

    @Test
    public void test() {
        try (MockedStatic<BizRuntimeContextRegistry> registry = mockStatic(BizRuntimeContextRegistry.class)) {
            ConcurrentHashMap<ClassLoader, BizRuntimeContext> runtimeMap = new ConcurrentHashMap<>();
            registry.when(BizRuntimeContextRegistry::getRuntimeMap).thenReturn(runtimeMap);

            Biz masterBiz = mockMastertBiz();
            Biz biz1 = mockBiz1();
            runtimeMap.put(masterBiz.getBizClassLoader(), new BizRuntimeContext(masterBiz));

            BizManagerServiceImpl bizManagerService = new BizManagerServiceImpl();
            bizManagerService.registerBiz(masterBiz);
            bizManagerService.registerBiz(biz1);

            BizRuntimeContext biz1RuntimeContext = new BizRuntimeContext(biz1);
            ApplicationContext biz1ApplicationContext = mock(ApplicationContext.class);
            HealthIndicator biz1HealthIndicator = mock(HealthIndicator.class);
            Map<String,HealthIndicator> biz1HealthIndicatorMap = new HashMap<>();
            biz1HealthIndicatorMap.put("biz1DiskSpaceHealthIndicator",biz1HealthIndicator);
            doReturn(biz1HealthIndicatorMap).when(biz1ApplicationContext).getBeansOfType(HealthIndicator.class);
            biz1RuntimeContext.setRootApplicationContext(biz1ApplicationContext);

            runtimeMap.put(biz1.getBizClassLoader(), biz1RuntimeContext);

            try (MockedStatic<ArkClient> arkClient = mockStatic(ArkClient.class)) {
                arkClient.when(ArkClient::getMasterBiz).thenReturn(masterBiz);
                arkClient.when(ArkClient::getBizManagerService).thenReturn(bizManagerService);

                doReturn(new Health.Builder().up().withDetail("cpuCount",1).build()).when(biz1HealthIndicator).health();
                assertEquals(Status.UP, indicator.health().getStatus());

                doReturn(new Health.Builder().down().withDetail("cpuCount",1).build()).when(biz1HealthIndicator).health();
                assertEquals(Status.DOWN, indicator.health().getStatus());

                doReturn(new Health.Builder().unknown().withDetail("cpuCount",1).build()).when(biz1HealthIndicator).health();
                assertEquals(Status.UNKNOWN, indicator.health().getStatus());
            }
        }
    }

    private Biz mockMastertBiz() {
        ClassLoader masterBizClassLoader = mock(ClassLoader.class);
        BizModel masterBiz = new BizModel();
        masterBiz.setClassLoader(masterBizClassLoader);
        masterBiz.setBizName("masterBiz");
        masterBiz.setBizVersion("1.0.0");
        masterBiz.setBizState(BizState.RESOLVED);
        return masterBiz;
    }

    private Biz mockBiz1() {
        ClassLoader bizClassLoader = mock(ClassLoader.class);
        BizModel biz = new BizModel();
        biz.setClassLoader(bizClassLoader);
        biz.setBizName("biz1");
        biz.setBizVersion("1.0.0");
        biz.setBizState(BizState.RESOLVED);
        return biz;
    }
}
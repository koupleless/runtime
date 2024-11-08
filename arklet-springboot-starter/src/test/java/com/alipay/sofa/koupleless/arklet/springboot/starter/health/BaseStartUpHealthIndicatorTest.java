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
import com.alipay.sofa.ark.spi.event.biz.AfterBizStartupEvent;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStopEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizStartupEvent;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStartupFailedEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizStopEvent;
import com.alipay.sofa.ark.spi.model.Biz;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: MasterBizStartUpHealthIndictor.java, v 0.1 2024年03月21日 15:05 立蓬 Exp $
 */
public class BaseStartUpHealthIndicatorTest {
    @Test
    public void testMasterBizStartUpHealthWithArkBiz() {
        Biz masterBiz = Mockito.mock(Biz.class);
        Biz biz1 = Mockito.mock(Biz.class);
        doReturn("biz1-v1").when(biz1).getIdentity();
        doReturn("masterBiz").when(masterBiz).getIdentity();

        try (MockedStatic<ArkClient> arkClient = Mockito.mockStatic(ArkClient.class)) {
            arkClient.when(ArkClient::getMasterBiz).thenReturn(masterBiz);

            BaseStartUpHealthIndicator indicator = new BaseStartUpHealthIndicator(true, 0);

            // case1-1: base starting:
            assertEquals(Status.UNKNOWN, indicator.health().getStatus());

            // case1-2: base startUp:
            indicator.onApplicationEvent(Mockito.mock(ApplicationReadyEvent.class));
            assertEquals(Status.UP, indicator.health().getStatus());

            // case1-3: base failed:
            indicator.onApplicationEvent(Mockito.mock(ApplicationFailedEvent.class));
            assertEquals(Status.DOWN, indicator.health().getStatus());

            // case2-1: base started && biz starting:
            indicator.onApplicationEvent(Mockito.mock(ApplicationReadyEvent.class));
            indicator.handleEvent(new BeforeBizStartupEvent(biz1));
            assertEquals(Status.UNKNOWN, indicator.health().getStatus());

            // case2-3: base started && biz startUp:
            indicator.handleEvent(new AfterBizStartupEvent(biz1));
            assertEquals(Status.UP, indicator.health().getStatus());

            // case2-4: base started && biz failed:
            indicator.handleEvent(new AfterBizStartupFailedEvent(biz1, new Throwable()));
            assertEquals(Status.DOWN, indicator.health().getStatus());

            // case2-5: base started && before biz stop:
            indicator.handleEvent(new BeforeBizStopEvent(biz1));
            assertEquals(Status.DOWN, indicator.health().getStatus());

            // case2-6: base started && biz stop:
            indicator.handleEvent(new AfterBizStopEvent(biz1));
            assertEquals(Status.UP, indicator.health().getStatus());
        }
    }

    @Test
    public void testMasterBizStartUpHealthWithArkBizAndSilence() {
        Biz masterBiz = Mockito.mock(Biz.class);
        Biz biz1 = Mockito.mock(Biz.class);
        doReturn("biz1-v1").when(biz1).getIdentity();
        doReturn("masterBiz").when(masterBiz).getIdentity();

        try (MockedStatic<ArkClient> arkClient = Mockito.mockStatic(ArkClient.class)) {
            arkClient.when(ArkClient::getMasterBiz).thenReturn(masterBiz);

            BaseStartUpHealthIndicator indicator = new BaseStartUpHealthIndicator(true, 1);

            // case1-1: base starting:
            assertEquals(Status.UNKNOWN, indicator.health().getStatus());

            // case1-2: base startUp:
            indicator.onApplicationEvent(Mockito.mock(ApplicationReadyEvent.class));
            assertEquals(Status.UP, indicator.health().getStatus());

            // case1-3: base failed:
            indicator.onApplicationEvent(Mockito.mock(ApplicationFailedEvent.class));
            assertEquals(Status.DOWN, indicator.health().getStatus());

            // case2-1: base started && biz starting:
            indicator.onApplicationEvent(Mockito.mock(ApplicationReadyEvent.class));
            indicator.handleEvent(new BeforeBizStartupEvent(biz1));
            assertEquals(Status.UNKNOWN, indicator.health().getStatus());

            // case2-3: base started && biz startUp:
            indicator.handleEvent(new AfterBizStartupEvent(biz1));
            assertEquals(Status.UP, indicator.health().getStatus());

            // case2-4: base started && biz failed:
            indicator.handleEvent(new AfterBizStartupFailedEvent(biz1, new Throwable()));
            assertEquals(Status.DOWN, indicator.health().getStatus());

            // case2-5: base started && before biz stop:
            indicator.handleEvent(new BeforeBizStopEvent(biz1));
            assertEquals(Status.DOWN, indicator.health().getStatus());

            // case2-6: base started && biz stop:
            indicator.handleEvent(new AfterBizStopEvent(biz1));
            assertEquals(Status.UP, indicator.health().getStatus());
        }
    }

    @Test
    public void testMasterBizStartUpHealthWithoutArkBiz() {
        Biz masterBiz = Mockito.mock(Biz.class);
        Biz biz1 = Mockito.mock(Biz.class);
        doReturn("biz1-v1").when(biz1).getIdentity();
        doReturn("masterBiz").when(masterBiz).getIdentity();

        try (MockedStatic<ArkClient> arkClient = Mockito.mockStatic(ArkClient.class)) {
            arkClient.when(ArkClient::getMasterBiz).thenReturn(masterBiz);

            BaseStartUpHealthIndicator indicator = new BaseStartUpHealthIndicator(false, 0);

            // case1-1: base starting:
            assertEquals(Status.UNKNOWN, indicator.health().getStatus());

            // case1-2: base startUp:
            indicator.onApplicationEvent(Mockito.mock(ApplicationReadyEvent.class));
            assertEquals(Status.UP, indicator.health().getStatus());

            // case1-3: base failed:
            indicator.onApplicationEvent(Mockito.mock(ApplicationFailedEvent.class));
            assertEquals(Status.DOWN, indicator.health().getStatus());

            // case2-1: base started && biz starting:
            indicator.onApplicationEvent(Mockito.mock(ApplicationReadyEvent.class));
            indicator.handleEvent(new BeforeBizStartupEvent(biz1));
            assertEquals(Status.UP, indicator.health().getStatus());

            // case2-3: base started && biz startUp:
            indicator.handleEvent(new AfterBizStartupEvent(biz1));
            assertEquals(Status.UP, indicator.health().getStatus());

            // case2-4: base started && biz failed:
            indicator.handleEvent(new AfterBizStartupFailedEvent(biz1, new Throwable()));
            assertEquals(Status.UP, indicator.health().getStatus());

            // case2-5: base started && biz stop:
            indicator.handleEvent(new AfterBizStopEvent(biz1));
            assertEquals(Status.UP, indicator.health().getStatus());
        }
    }
}

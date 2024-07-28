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
import com.alipay.sofa.ark.spi.event.biz.AfterBizFailedEvent;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStopEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizStartupEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizStopEvent;
import com.alipay.sofa.ark.spi.model.Biz;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationContext;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: BaseProbeAvailabilityStateHandlerTest.java, v 0.1 2024年07月28日 21:55 立蓬 Exp $
 */
public class BaseProbeAvailabilityStateHandlerTest {

    @Test
    public void testWithoutAllBizReadiness() {
        Biz masterBiz = Mockito.mock(Biz.class);
        Biz biz1 = Mockito.mock(Biz.class);
        doReturn("biz1-v1").when(biz1).getIdentity();
        doReturn("masterBiz").when(masterBiz).getIdentity();

        ApplicationAvailability baseAvailability = Mockito.mock(ApplicationAvailability.class);
        when(baseAvailability.getReadinessState()).thenReturn(ReadinessState.ACCEPTING_TRAFFIC);
        when(baseAvailability.getLivenessState()).thenReturn(LivenessState.CORRECT);

        ApplicationContext baseContext = Mockito.mock(ApplicationContext.class);
        when(baseContext.getBean("applicationAvailability")).thenReturn(baseAvailability);
        doNothing().when(baseContext).publishEvent(ArgumentMatchers.any());

        BaseProbeAvailabilityStateHandler handler = new BaseProbeAvailabilityStateHandler(
            baseContext, false);

        try (MockedStatic<ArkClient> arkClient = Mockito.mockStatic(ArkClient.class)) {
            arkClient.when(ArkClient::getMasterBiz).thenReturn(masterBiz);

            handler.handleEvent(new BeforeBizStartupEvent(biz1));

            verify(baseContext, times(0)).publishEvent(ArgumentMatchers.any());
        }
    }

    @Test
    public void testWithAllBizReadiness() {
        Biz masterBiz = Mockito.mock(Biz.class);
        Biz biz1 = Mockito.mock(Biz.class);
        doReturn("biz1-v1").when(biz1).getIdentity();
        doReturn("masterBiz").when(masterBiz).getIdentity();

        ApplicationAvailability baseAvailability = Mockito.mock(ApplicationAvailability.class);
        when(baseAvailability.getReadinessState()).thenReturn(ReadinessState.ACCEPTING_TRAFFIC);
        when(baseAvailability.getLivenessState()).thenReturn(LivenessState.CORRECT);

        ApplicationContext baseContext = Mockito.mock(ApplicationContext.class);
        when(baseContext.getBean("applicationAvailability")).thenReturn(baseAvailability);
        doNothing().when(baseContext).publishEvent(ArgumentMatchers.any());

        BaseProbeAvailabilityStateHandler handler = new BaseProbeAvailabilityStateHandler(
            baseContext, true);

        try (MockedStatic<ArkClient> arkClient = Mockito.mockStatic(ArkClient.class)) {
            arkClient.when(ArkClient::getMasterBiz).thenReturn(masterBiz);

            handler.handleEvent(new BeforeBizStartupEvent(biz1));

            handler.handleEvent(new AfterBizFailedEvent(biz1, new Throwable()));

            handler.handleEvent(new AfterBizStopEvent(biz1));

            handler.handleEvent(new BeforeBizStopEvent(biz1));

            handler.handleEvent(new AfterBizStopEvent(biz1));

            verify(baseContext, times(3)).publishEvent(ArgumentMatchers.any());
        }
    }
}
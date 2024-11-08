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
package com.alipay.sofa.koupleless.ext.autoconfigure.web.gateway;

import com.alipay.sofa.ark.spi.event.biz.AfterBizStartupEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizStopEvent;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.koupleless.common.util.ArkUtils;
import com.alipay.sofa.koupleless.common.util.SpringUtils;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: CompositeBizForwardsHandlerTest.java, v 0.1 2024年11月08日 20:24 立蓬 Exp $
 */
public class CompositeBizForwardsHandlerTest {

    @Test
    public void testCompositeBizForwardsHandler() {
        CompositeBizForwardsHandler handler = new CompositeBizForwardsHandler();

        try (MockedStatic<ArkUtils> arkUtils = Mockito.mockStatic(ArkUtils.class)) {
            // case1: master biz
            arkUtils.when(ArkUtils::isMasterBiz).thenReturn(true);
            handler.handleEvent(null);

            // case2: biz
            arkUtils.when(ArkUtils::isMasterBiz).thenReturn(false);
            Biz biz = Mockito.mock(Biz.class);

            try (MockedStatic<SpringUtils> springUtils = Mockito.mockStatic(SpringUtils.class)) {
                // case2.1: get bean null
                springUtils.when(() -> SpringUtils.getBean(biz, "forwards")).thenReturn(null);

                handler.handleEvent(new AfterBizStartupEvent(biz));
                assertTrue(CompositeBizForwardsHandler.getBizForwards().isEmpty());

                handler.handleEvent(new BeforeBizStopEvent(biz));
                assertTrue(CompositeBizForwardsHandler.getBizForwards().isEmpty());

                // case2.2: get bean not null
                springUtils.when(() -> SpringUtils.getBean(biz, "forwards"))
                    .thenReturn(new Forwards());

                handler.handleEvent(new AfterBizStartupEvent(biz));
                assertEquals(1, CompositeBizForwardsHandler.getBizForwards().size());

                handler.handleEvent(new BeforeBizStopEvent(biz));
                assertTrue(CompositeBizForwardsHandler.getBizForwards().isEmpty());

            }
        }

    }
}
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
package com.alipay.sofa.koupleless.common;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.koupleless.common.model.MainApplication;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: MainBizApplicationTest.java, v 0.1 2024年08月26日 11:01 立蓬 Exp $
 */
public class MainBizApplicationTest {
    @Test
    public void test() {
        try (MockedStatic<BizRuntimeContextRegistry> mockedStatic = Mockito
            .mockStatic(BizRuntimeContextRegistry.class)) {
            BizRuntimeContext bizRuntimeContext = new BizRuntimeContext(mock(Biz.class));
            mockedStatic
                .when(() -> BizRuntimeContextRegistry.getBizRuntimeContextByClassLoader(
                    Thread.currentThread().getContextClassLoader()))
                .thenAnswer(invocation -> bizRuntimeContext);

            MainApplication.init();
            MainApplication.register("testObject", new TestObject());

            assertEquals("testObj",
                ((TestObject) MainApplication.getObject("testObject")).getName());

            Map<String, TestObject> testObjectMap = MainApplication.getObjectMap(TestObject.class);
            assertEquals(2, testObjectMap.size());
            assertTrue(testObjectMap.containsKey("testObject"));
            assertTrue(testObjectMap.containsKey(
                "com.alipay.sofa.koupleless.common.MainBizApplicationTest$TestObject"));
            assertEquals(testObjectMap.get("testObject"), testObjectMap
                .get("com.alipay.sofa.koupleless.common.MainBizApplicationTest$TestObject"));

            Map<String, TestFacade> testFacadeMap = MainApplication.getObjectMap(TestFacade.class);
            assertEquals(2, testFacadeMap.size());
            assertTrue(testFacadeMap.containsKey("testObject"));
            assertTrue(testFacadeMap.containsKey(
                "com.alipay.sofa.koupleless.common.MainBizApplicationTest$TestObject"));
            assertEquals(testFacadeMap.get("testObject"), testFacadeMap
                .get("com.alipay.sofa.koupleless.common.MainBizApplicationTest$TestObject"));

            bizRuntimeContext.getApplicationContext().close();
        }

    }

    private static class TestObject implements TestFacade {
        @Override
        public String getName() {
            return "testObj";
        }
    }

    private interface TestFacade {
        String getName();
    }
}
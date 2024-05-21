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
package com.alipay.sofa.koupleless.test.suite.spring.multi;

import com.alipay.sofa.koupleless.test.suite.common.IntegrationLogger;
import com.alipay.sofa.koupleless.test.suite.spring.mock.common.HelloService;
import com.alipay.sofa.koupleless.test.suite.spring.model.BaseSpringTestConfig;
import com.alipay.sofa.koupleless.test.suite.spring.model.BizSpringTestConfig;
import com.alipay.sofa.koupleless.test.suite.spring.model.MultiSpringTestConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author CodeNoobKing
 * @since 2024/3/11
 */
public class TestMultiSSpringApplicationTest {

    public void testMultiApplicationLaunched() throws Throwable {
        IntegrationLogger.getLogger();
        System.setProperty("koupleless.module.initializer.skip", "");

        BaseSpringTestConfig baseConfig = BaseSpringTestConfig.builder()
            .mainClass(com.alipay.sofa.koupleless.test.suite.spring.mock.base.Application.class)
            .build();

        List<BizSpringTestConfig> bizConfigs = new ArrayList<>();
        bizConfigs.add(BizSpringTestConfig.builder().bizName("biz0")
            .mainClass(com.alipay.sofa.koupleless.test.suite.spring.mock.biz.Application.class)
            .build());

        TestMultiSpringApplication application = new TestMultiSpringApplication(
            MultiSpringTestConfig.builder().baseConfig(baseConfig).bizConfigs(bizConfigs).build());

        application.run();
        Thread.sleep(1_000);

        HelloService sampleBaseService = application.getBaseApplication().getApplicationContext()
            .getBean(HelloService.class);

        Assert.assertEquals(Thread.currentThread().getContextClassLoader().getClass().getName(),
            sampleBaseService.helloWorld());

        HelloService sampleBizService = application.getBizApplication("biz0")
            .getApplicationContext().getBean(HelloService.class);

        Assert.assertEquals("com.alipay.sofa.koupleless.test.suite.biz.TestBizClassLoader",
            sampleBizService.getClass().getClassLoader().getClass().getName());
    }
}

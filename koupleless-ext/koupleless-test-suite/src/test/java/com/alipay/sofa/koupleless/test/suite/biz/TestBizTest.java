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
package com.alipay.sofa.koupleless.test.suite.biz;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author CodeNoobKing
 * @since 2024/2/20
 */
public class TestBizTest {

    private static TestBizModel testBiz;

    @BeforeClass
    public static void setUp() {
        TestBootstrap.init(TestBizTest.class.getClassLoader());

        List<String> testClassNames = new ArrayList<>();
        testClassNames.add("com.alipay.sofa.koupleless.test.suite.mock.LoadByTestBizClassA");
        testClassNames.add("com.alipay.sofa.koupleless.test.suite.mock.LoadByTestBizClassB");
        List<String> includeClassPatterns = new ArrayList<>();

        testBiz = new TestBizModel(TestBizConfig.builder()
            .bootstrapClassName("com.alipay.sofa.koupleless.test.suite.mock.BootStrapClass")
            .bizName("test").bizVersion("1.0.0").testClassNames(testClassNames)
            .includeClassPatterns(includeClassPatterns)
            .baseClassLoader((URLClassLoader) TestBizTest.class.getClassLoader()).build());
    }

    @Test
    public void testTestBiz() {

        {
            Assert.assertTrue(testBiz.isDeclaredMode());
            Assert.assertTrue(testBiz.isDeclared(null, null));
        }

        Assert.assertEquals(2, testBiz.getTestClasses().size());

        testBiz.executeTest(() -> {
            Assert.assertTrue(
                Thread.currentThread().getContextClassLoader() instanceof TestBizClassLoader);
        });
    }
}

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
package com.alipay.sofa.koupleless.common.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for {@link IsolatedEnvironment}.
 *
 * @author oxsean@gmail.com
 * @version 1.0.0
 */
public class IsolatedEnvironmentTest {

    private ClassLoader baseClassLoader;

    @Before
    public void before() {
        Thread thread = Thread.currentThread();
        baseClassLoader = thread.getContextClassLoader();
        Map<String, String> env = new HashMap<>();
        env.put("key1", "value1");
        env.put("app1_key1", "value2");
        IsolatedEnvironment.takeover(TestClassLoader.class.getName(), env);
    }

    @After
    public void after() {
        Thread.currentThread().setContextClassLoader(baseClassLoader);
    }

    @Test
    public void takeover() {
        Assert.assertEquals("value1", System.getenv("key1"));
        Thread.currentThread().setContextClassLoader(new TestClassLoader());
        Assert.assertEquals("value2", System.getenv("key1"));
    }

    private static class TestClassLoader extends URLClassLoader {
        public TestClassLoader() {
            super(new URL[0]);
        }

        public BizModel getBizModel() {
            return new BizModel();
        }
    }

    private static class BizModel {

        public String getBizName() {
            return "app1";
        }
    }
}
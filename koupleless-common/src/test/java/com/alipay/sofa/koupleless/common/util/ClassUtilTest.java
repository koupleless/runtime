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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ClassUtilTest.java, v 0.1 2024年05月14日 00:05 立蓬 Exp $
 */
public class ClassUtilTest {
    @Test
    public void testSetField() {
        TestClassA testClassA = new TestClassA();
        ClassUtil.setField("name", testClassA, "b");
        ClassUtil.setField("parentName", testClassA, "child");
        assertEquals("b", testClassA.getName());
        assertEquals("child", testClassA.getParentName());
    }

    class TestClassParent {
        private String parentName = "parent";

        String getParentName() {
            return parentName;
        }
    }

    class TestClassA extends TestClassParent {
        private String name = "a";

        String getName() {
            return name;
        }
    }
}
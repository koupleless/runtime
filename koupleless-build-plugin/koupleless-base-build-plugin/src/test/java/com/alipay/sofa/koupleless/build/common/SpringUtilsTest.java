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
package com.alipay.sofa.koupleless.build.common;

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author CodeNoobKing
 * @date 2024/3/18
 **/
public class SpringUtilsTest {

    private static SpringUtils INSTANCE = new SpringUtils();

    @Test
    public void testParseSpringFactoryConfig() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
            "spring.factory.example");
        Map<String, List<String>> keyToImpls = INSTANCE.parseSpringFactoryConfig(inputStream);

        ArrayList<String> expectedImpls = new ArrayList<>();
        expectedImpls.add("org.example.0");
        expectedImpls.add("org.example.1");
        expectedImpls.add("org.example.2");

        Map<String, List<String>> expected = new HashMap<>();
        expected.put("key0", expectedImpls);
        expected.put("key1", expectedImpls);
        expected.put("key2", expectedImpls);

        Assert.assertEquals(expected, keyToImpls);
    }
}

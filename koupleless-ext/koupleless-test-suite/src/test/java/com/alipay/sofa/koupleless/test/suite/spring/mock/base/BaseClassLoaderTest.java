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
package com.alipay.sofa.koupleless.test.suite.spring.mock.base;

import com.alipay.sofa.koupleless.test.suite.spring.base.BaseClassLoader;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.List;

/**
 * @author CodeNoobKing
 * @since 2024/3/28
 **/
public class BaseClassLoaderTest {

    @Test
    public void testGetUrlsFromSurefireManifest() {
        URL url = getClass().getClassLoader().getResource("surefirebooter.jar");
        List<URL> urlsFromSurefireManifest = BaseClassLoader.getUrlsFromSurefireManifest(url);
        Assert.assertEquals(200, urlsFromSurefireManifest.size());
        Assert.assertTrue(urlsFromSurefireManifest.stream()
            .anyMatch(it -> it.getPath().endsWith("test-classes/")));
        Assert.assertTrue(urlsFromSurefireManifest.stream()
            .anyMatch(it -> it.getPath().endsWith("target/classes/")));

    }

    @Test
    public void getUrlsFromSurefireManifestWithoutFilePrefix() {
        URL url = getClass().getClassLoader().getResource("surefirebooter_without_file_prefix.jar");
        List<URL> urlsFromSurefireManifest = BaseClassLoader.getUrlsFromSurefireManifest(url);
        Assert.assertEquals(184, urlsFromSurefireManifest.size());
        Assert.assertTrue(urlsFromSurefireManifest.stream()
            .anyMatch(it -> it.getPath().endsWith("biz2-web-single-host/target/classes/")));
        Assert.assertTrue(urlsFromSurefireManifest.stream()
            .anyMatch(it -> it.getPath().endsWith("biz1-web-single-host/target/classes/")));
    }
}

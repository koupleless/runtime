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
package com.alipay.sofa.koupleless.test.suite;

import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderService;
import com.alipay.sofa.koupleless.test.suite.biz.SOFAArkTestClassLoaderHook;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author CodeNoobKing
 * @date 2024/3/21
 **/
@RunWith(MockitoJUnitRunner.class)
public class SOFAArkTestClassLoaderHookTest {

    private SOFAArkTestClassLoaderHook sofaArkTestClassLoaderHook = new SOFAArkTestClassLoaderHook();

    @Test
    public void testLoadResourceInsideJar() throws Throwable {
        List<String> artifacts = new ArrayList<>();
        artifacts.add("demo-executable");

        URL resource = getClass().getClassLoader().getResource("demo-executable.jar");
        sofaArkTestClassLoaderHook.putHigherPriorityResourceArtifacts("test:TEST", artifacts);

        ClassLoaderService classLoaderService = mock(ClassLoaderService.class);
        doReturn(new URLClassLoader(new URL[] { resource }),
            Thread.currentThread().getContextClassLoader()).when(classLoaderService)
                .getMasterBizClassLoader();

        URL url = sofaArkTestClassLoaderHook.preFindResource("META-INF/MANIFEST.MF",
            classLoaderService, new BizModel().setBizName("test").setBizVersion("TEST"));

        InputStream inputStream = url.openConnection().getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);
        List<String> lines = IOUtils.readLines(reader);
        Assert.assertTrue(lines.contains("Start-Class: com.example.demo.DemoApplication"));
    }

    @Test
    public void testLoadResourceInsideBuildDirectory() throws Throwable {
        SOFAArkTestClassLoaderHook.setBuildDirectory("testtarget/classes");

        List<String> artifacts = new ArrayList<>();
        artifacts.add("project");

        Path dir = Paths
            .get(new File(getClass().getClassLoader().getResource("demo-executable.jar").getPath())
                .getParent(), "project", "testtarget", "classes");


        sofaArkTestClassLoaderHook.putHigherPriorityResourceArtifacts("test:TEST", artifacts);

        ClassLoaderService classLoaderService = mock(ClassLoaderService.class);
        doReturn(new URLClassLoader(new URL[] { new File(dir.toString()).toURI().toURL() }),
            Thread.currentThread().getContextClassLoader()).when(classLoaderService)
                .getMasterBizClassLoader();

        URL url = sofaArkTestClassLoaderHook.preFindResource("META-INF/MANIFEST.MF",
            classLoaderService, new BizModel().setBizName("test").setBizVersion("TEST"));

        InputStream inputStream = url.openConnection().getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);
        List<String> lines = IOUtils.readLines(reader);
        Assert.assertTrue(lines.contains("HELLO: WORLD"));
    }
}

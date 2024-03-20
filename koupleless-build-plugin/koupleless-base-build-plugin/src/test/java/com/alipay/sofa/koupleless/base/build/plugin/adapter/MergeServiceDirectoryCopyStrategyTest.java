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
package com.alipay.sofa.koupleless.base.build.plugin.adapter;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author CodeNoobKing
 * @date 2024/3/18
 **/
public class MergeServiceDirectoryCopyStrategyTest {

    @Test
    public void testMergeServiceDirectory() throws Throwable {
        File buildDir = Files.createTempDirectory("testDir").toFile();
        File buildDirMETA = Files.createDirectories(
            Paths.get(buildDir.getAbsolutePath(), "META-INF", "services")).toFile();
        File buildDirTemplate = new File(getClass().getClassLoader()
            .getResource("testcopy/services0").toURI());
        File adapterDir = new File(getClass().getClassLoader().getResource("testcopy/services1")
            .toURI());
        MergeServiceDirectoryCopyStrategy strategy = new MergeServiceDirectoryCopyStrategy();
        List<String> fileNames = Arrays.asList(buildDirTemplate.list());
        for (String fileName : fileNames) {
            strategy.copy(buildDir, "META-INF/services/" + fileName,
                Files.readAllBytes(Paths.get(buildDirTemplate.getPath(), fileName)));
        }

        FileUtils.copyDirectory(buildDirTemplate, buildDirMETA);

        fileNames = Arrays.asList(adapterDir.list());
        for (String fileName : fileNames) {
            strategy.copy(buildDir, "META-INF/services/" + fileName,
                Files.readAllBytes(Paths.get(adapterDir.getPath(), fileName)));
        }

        System.out.println(fileNames);
        Assert.assertEquals(4, fileNames.size());
        Assert.assertTrue(fileNames.contains("org.apache.dubbo.common.context.FrameworkExt"));
        Assert.assertTrue(fileNames.contains("org.apache.dubbo.rpc.Filter"));
        Assert.assertTrue(fileNames.contains("org.foo.bar"));

        List<String> lines = Files.readAllLines(Paths.get(buildDirMETA.getAbsolutePath(),
            "org.apache.dubbo.common.context.FrameworkExt"));
        List<String> expected = new ArrayList<>();
        expected.add("com.alipay.sofa.koupleless.support.dubbo.KouplelessConfigManager");
        expected.add("com.alipay.sofa.koupleless.support.dubbo.KouplelessServiceRepository");

        Assert.assertEquals(expected, lines);

        lines = Files.readAllLines(Paths.get(buildDirMETA.getAbsolutePath(),
            "org.apache.dubbo.rpc.Filter"));
        expected.clear();
        expected.add("com.alipay.sofa.koupleless.support.dubbo.ConsumerRedefinePathFilter0");
        expected.add("com.alipay.sofa.koupleless.support.dubbo.ConsumerRedefinePathFilter1");
        expected.add("com.alipay.sofa.koupleless.support.dubbo.ConsumerRedefinePathFilter2");
        Assert.assertEquals(expected, lines);
    }
}

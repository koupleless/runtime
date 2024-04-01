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

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author CodeNoobKing
 * @date 2024/3/18
 **/
public class CopyAdapterStrategyTest {

    @Test
    public void testDirectCopy() throws Throwable {
        File adapterFile = new File(
            getClass().getClassLoader().getResource("testcopy/file0").toURI());
        File buildDir = Files.createTempDirectory("test").toFile();
        ClassCopyStrategy directCopyStrategy = new ClassCopyStrategy();
        directCopyStrategy.copy(buildDir, "example/file0",
            Files.readAllBytes(adapterFile.toPath()));

        byte[] bytes = Files
            .readAllBytes(Paths.get(buildDir.toPath().toString(), "example", "file0"));
        Assert.assertEquals("hello world!", new String(bytes));
    }
}

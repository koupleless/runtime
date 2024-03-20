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

import lombok.SneakyThrows;

import java.io.File;

/**
 * @author CodeNoobKing
 * @date 2024/3/20
 **/
public class AdapterCopyService {

    private ClassCopyStrategy                    classCopyStrategy                    = new ClassCopyStrategy();
    private MergeServiceDirectoryCopyStrategy    mergeServiceDirectoryCopyStrategy    = new MergeServiceDirectoryCopyStrategy();
    private MergeSpringFactoryConfigCopyStrategy mergeSpringFactoryConfigCopyStrategy = new MergeSpringFactoryConfigCopyStrategy();

    @SneakyThrows
    public void copy(File buildDir, String entryName, byte[] content) {
        if (entryName.endsWith(".class")) {
            classCopyStrategy.copy(buildDir, entryName, content);
        } else if (entryName.startsWith("META-INF/services")) {
            mergeServiceDirectoryCopyStrategy.copy(buildDir, entryName, content);
        } else if (entryName.equals("META-INF/spring.factories")) {
            mergeSpringFactoryConfigCopyStrategy.copy(buildDir, entryName, content);
        }
    }
}

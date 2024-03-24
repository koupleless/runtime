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

import com.alipay.sofa.koupleless.build.common.SpringUtils;
import org.codehaus.plexus.util.CollectionUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author CodeNoobKing
 * @date 2024/3/18
 **/
public class MergeSpringFactoryConfigCopyStrategy implements CopyAdapterStrategy {

    public void mergeSpringFactories(Map<String, List<String>> adapterConfig,
                                     Map<String, List<String>> buildConfig) {
        for (Map.Entry<String, List<String>> entry : adapterConfig.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            if (buildConfig.containsKey(key)) {
                List<String> mergedValue = new ArrayList<>(buildConfig.get(key));
                // only add the new values
                mergedValue.addAll(CollectionUtils.subtract(value, buildConfig.get(key)));
                buildConfig.put(key, mergedValue);
            } else {
                buildConfig.put(key, value);
            }
        }
    }

    public List<String> formatSpringFactoryConfig(Map<String, List<String>> config) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : config.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            result.add(key + "=" + String.join(",", value));
        }
        return result;
    }

    @Override
    public void copy(File buildDir, String entryName, byte[] content) throws Throwable {
        File factoryFile = new File(Paths.get(buildDir.getAbsolutePath(), "META-INF",
            "spring.factories").toUri());
        if (!factoryFile.exists()) {
            Files.createDirectories(factoryFile.toPath().getParent());
            Files.createFile(factoryFile.toPath());
        }

        Map<String, List<String>> adapterConfig = SpringUtils.INSTANCE().parseSpringFactoryConfig(
            new ByteArrayInputStream(content));

        InputStream buildIS = Files.newInputStream(factoryFile.toPath());
        Map<String, List<String>> buildConfig = SpringUtils.INSTANCE().parseSpringFactoryConfig(
            buildIS);

        mergeSpringFactories(adapterConfig, buildConfig);
        List<String> lines = formatSpringFactoryConfig(buildConfig);
        Files.write(factoryFile.toPath(), lines, StandardOpenOption.TRUNCATE_EXISTING);
    }

}

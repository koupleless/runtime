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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * @author CodeNoobKing
 * @date 2024/3/18
 **/
public class MergeServiceDirectoryCopyStrategy implements CopyAdapterStrategy {

    public List<String> mergeContent(byte[] inputContent, byte[] outputContent) throws Throwable {
        // convert byte[] to Reader
        List<String> mergedLines = null;
        InputStreamReader inputReader = new InputStreamReader(
            new ByteArrayInputStream(inputContent));
        List<String> adapterLines = IOUtils.readLines(inputReader);

        InputStreamReader outputReader = new InputStreamReader(
            new ByteArrayInputStream(outputContent));
        List<String> originalLines = IOUtils.readLines(outputReader);

        mergedLines = new ArrayList<>(originalLines);
        Collection<String> deltaLines = CollectionUtils.subtract(adapterLines, originalLines);
        mergedLines.addAll(deltaLines);
        return mergedLines;
    }

    @Override
    public void copy(File buildDir, String entryName, byte[] content) throws Throwable {
        File serviceDir = Paths.get(buildDir.getAbsolutePath(), "META-INF", "services").toFile();
        if (!serviceDir.exists()) {
            Files.createDirectories(serviceDir.toPath());
        }
        String configName = StringUtils.removeStart(entryName, "META-INF/services");
        File originalConfigFile = Paths.get(serviceDir.getAbsolutePath(), configName).toFile();
        if (!originalConfigFile.exists()) {
            Files.createFile(originalConfigFile.toPath());
        }

        byte[] originalBytes = Files.readAllBytes(originalConfigFile.toPath());
        List<String> mergedLines = mergeContent(content, originalBytes);
        Files.write(originalConfigFile.toPath(), mergedLines, TRUNCATE_EXISTING);
    }
}

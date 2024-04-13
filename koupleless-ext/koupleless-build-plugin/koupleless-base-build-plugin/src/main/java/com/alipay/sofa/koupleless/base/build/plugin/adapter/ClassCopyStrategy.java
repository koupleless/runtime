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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author CodeNoobKing
 * @date 2024/3/18
 **/
public class ClassCopyStrategy implements CopyAdapterStrategy {
    /**
     * directly copy the file from in to out
     */
    @Override
    public void copy(File buildDir, String entryName, byte[] content) throws Throwable {
        File fileToCreate = Paths.get(buildDir.getAbsolutePath(), entryName).toFile();
        Files.createDirectories(fileToCreate.toPath().getParent());
        if (!fileToCreate.exists()) {
            Files.createFile(fileToCreate.toPath());
        }

        // byte to input stream
        Files.write(fileToCreate.toPath(), content, StandardOpenOption.TRUNCATE_EXISTING);
    }
}

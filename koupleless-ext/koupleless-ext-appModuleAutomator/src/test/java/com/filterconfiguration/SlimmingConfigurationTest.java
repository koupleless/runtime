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
package com.filterconfiguration;

import com.auto_module_upgrade.filterconfiguration.SlimmingConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SlimmingConfigurationTest {

    @TempDir
    Path tempDir;

    @Test
    void testCreateBootstrapProperties() throws Exception {
        Path targetDir = tempDir.resolve("conf").resolve("ark");
        Files.createDirectories(targetDir);
        String fileName = "bootstrap.properties";

        SlimmingConfiguration.createBootstrapProperties(targetDir.toString(), fileName);

        Path propertiesFile = targetDir.resolve(fileName);
        assertTrue(Files.exists(propertiesFile), "bootstrap.properties 文件应该被创建");

        List<String> lines = Files.readAllLines(propertiesFile);
        assertTrue(lines.stream().anyMatch(line -> line.startsWith("excludeGroupIds=")), "应该包含 excludeGroupIds 配置");
        assertTrue(lines.stream().anyMatch(line -> line.startsWith("excludeArtifactIds=")), "应该包含 excludeArtifactIds 配置");
    }
}
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
package com.alipay.sofa.koupleless.applicationPropertiesModifier;

import com.alipay.sofa.koupleless.auto_module_upgrade.applicationPropertiesModifier.ApplicationPropertiesModifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationPropertiesModifierTest {

    @TempDir
    Path tempDir;

    @Test
    void testModifyApplicationProperties() throws Exception {
        String applicationName = "TestApp";
        Path propertiesFile = tempDir.resolve("application.properties");
        Files.write(propertiesFile, "some.property=value".getBytes(StandardCharsets.UTF_8));

        ApplicationPropertiesModifier.modifyApplicationProperties(tempDir.toString(), applicationName);

        List<String> lines = Files.readAllLines(propertiesFile, StandardCharsets.UTF_8);
        assertTrue(lines.contains("spring.application.name=" + applicationName), "应该添加 spring.application.name 属性");
        assertTrue(lines.contains("some.property=value"), "不应修改现有属性");
    }
}
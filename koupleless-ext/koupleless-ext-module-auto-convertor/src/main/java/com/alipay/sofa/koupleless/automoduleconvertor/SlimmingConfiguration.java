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
package com.alipay.sofa.koupleless.automoduleconvertor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

public class SlimmingConfiguration {
    private static final Logger logger      = LoggerFactory.getLogger(SlimmingConfiguration.class);
    private static final String CONFIG_FILE = "config.properties";

    public static void createBootstrapProperties(String targetDirectoryPath, String fileName) {
        Map<String, String> completeConfigs = loadSlimmingConfigurations();

        Path directory = Paths.get(targetDirectoryPath);
        Path propertiesFile = directory.resolve(fileName);

        try {
            Files.createDirectories(directory);
            logger.info("目录已创建: {}", targetDirectoryPath);

            List<String> existingLines = Files.exists(propertiesFile)
                ? Files.readAllLines(propertiesFile)
                : new ArrayList<>();
            Map<String, String> existingConfigs = parseExistingConfigs(existingLines);

            List<String> updatedLines = updateConfigs(existingConfigs, completeConfigs);

            Files.write(propertiesFile, updatedLines);
            logger.info("配置文件已更新: {}", propertiesFile);
        } catch (IOException e) {
            logger.error("创建或更新配置文件时发生错误", e);
        }
    }

    private static Map<String, String> loadSlimmingConfigurations() {
        Properties props = new Properties();
        try (InputStream input = SlimmingConfiguration.class.getClassLoader()
            .getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                logger.error("无法找到 {} 文件", CONFIG_FILE);
                return Collections.emptyMap();
            }
            props.load(input);
        } catch (IOException e) {
            logger.error("读取配置文件时发生错误", e);
            return Collections.emptyMap();
        }

        Map<String, String> configs = new LinkedHashMap<>();
        configs.put("excludeGroupIds", props.getProperty("slimming.excludeGroupIds", ""));
        configs.put("excludeArtifactIds", props.getProperty("slimming.excludeArtifactIds", ""));
        return configs;
    }

    private static Map<String, String> parseExistingConfigs(List<String> lines) {
        Map<String, String> configs = new LinkedHashMap<>();
        for (String line : lines) {
            if (line.contains("=")) {
                String[] parts = line.split("=", 2);
                String key = parts[0].trim();
                String value = parts.length > 1 ? parts[1].trim() : "";
                configs.put(key, value);
            }
        }
        return configs;
    }

    private static String mergeConfigValues(String existingValue, String completeValue) {
        Set<String> items = new LinkedHashSet<>();
        if (!existingValue.isEmpty()) {
            items.addAll(Arrays.asList(existingValue.split(",")));
        }
        items.addAll(Arrays.asList(completeValue.split(",")));
        return String.join(",", items);
    }

    private static List<String> updateConfigs(Map<String, String> existingConfigs,
                                              Map<String, String> completeConfigs) {
        List<String> updatedLines = new ArrayList<>();
        for (Map.Entry<String, String> entry : completeConfigs.entrySet()) {
            String key = entry.getKey();
            String completeValue = entry.getValue();
            String existingValue = existingConfigs.get(key);
            if (existingValue == null) {
                existingValue = "";
            }

            String updatedValue = mergeConfigValues(existingValue, completeValue);
            updatedLines.add(key + "=" + updatedValue);
        }
        return updatedLines;
    }

    public static String getSofaArkVersion() {
        String version = getVersionFromRootPom("sofa.ark.version");
        return version != null ? version : "${sofa.ark.version}";
    }

    public static String getKouplelessRuntimeVersion() {
        String version = getVersionFromRootPom("revision");
        return version != null ? version : "${revision}";
    }

    private static String getVersionFromRootPom(String propertyName) {
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            File pomFile = findRootPomFile();
            if (pomFile == null) {
                logger.error("无法找到根 pom.xml 文件");
                return null;
            }
            Model model = reader.read(new FileReader(pomFile));
            String version = model.getProperties().getProperty(propertyName);
            if (version == null && "revision".equals(propertyName)) {
                version = model.getVersion();
            }
            if (version == null && model.getParent() != null) {
                version = model.getParent().getVersion();
            }
            return version;
        } catch (Exception e) {
            logger.error("无法读取 " + propertyName, e);
            return null;
        }
    }

    private static File findRootPomFile() {
        File currentDir = new File(System.getProperty("user.dir"));
        while (currentDir != null) {
            File pomFile = new File(currentDir, "pom.xml");
            if (pomFile.exists()) {
                try {
                    MavenXpp3Reader reader = new MavenXpp3Reader();
                    Model model = reader.read(new FileReader(pomFile));
                    if ("koupleless-runtime".equals(model.getArtifactId())
                        || (model.getParent() != null
                            && "koupleless-runtime".equals(model.getParent().getArtifactId()))) {
                        return pomFile;
                    }
                } catch (Exception e) {
                    logger.error("读取 pom.xml 文件时发生错误", e);
                }
            }
            currentDir = currentDir.getParentFile();
        }
        return null;
    }
}

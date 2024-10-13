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
package com.alipay.sofa.koupleless.auto_module_upgrade.applicationPropertiesModifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;


public class ApplicationPropertiesModifier {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationPropertiesModifier.class);
    private static final String APPLICATION_PROPERTIES = "application.properties";
    private static final String SPRING_APPLICATION_NAME = "spring.application.name";

    public static void modifyApplicationProperties(String directoryPath, String applicationName) throws IOException {
        Path directory = Paths.get(directoryPath);
        scanDirectory(directory, applicationName);
    }

    private static void scanDirectory(Path directory, String applicationName) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    scanDirectory(path, applicationName);
                } else if (isApplicationPropertiesFile(path)) {
                    modifySpringApplicationName(path, applicationName);
                } else if (isResourcesDirectory(path)) {
                    createApplicationPropertiesIfNotExists(path, applicationName);
                }
            }
        }
    }

    private static boolean isApplicationPropertiesFile(Path path) {
        return path.getFileName().toString().equalsIgnoreCase(APPLICATION_PROPERTIES);
    }

    private static boolean isResourcesDirectory(Path path) {
        return Files.isDirectory(path) && path.getFileName().toString().equalsIgnoreCase("resources");
    }

    private static void modifySpringApplicationName(Path path, String applicationName) throws IOException {
        Properties props = new Properties();
        boolean fileExists = Files.exists(path);

        if (fileExists) {
            try (InputStream input = Files.newInputStream(path)) {
                props.load(input);
            }
        }

        props.setProperty(SPRING_APPLICATION_NAME, applicationName);

        try (OutputStream output = Files.newOutputStream(path)) {
            props.store(output, null);
        }

        logger.info("{} application.properties 文件: {}", fileExists ? "已修改" : "已创建", path);
    }

    private static void createApplicationPropertiesIfNotExists(Path directory, String applicationName) throws IOException {
        Path propertiesPath = directory.resolve(APPLICATION_PROPERTIES);
        if (!Files.exists(propertiesPath)) {
            modifySpringApplicationName(propertiesPath, applicationName);
        }
    }
}
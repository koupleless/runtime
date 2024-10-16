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
package com.alipay.sofa.koupleless.autolauncher;

import com.alipay.sofa.koupleless.automoduleconvertor.ApplicationPropertiesModifier;
import com.alipay.sofa.koupleless.automoduleconvertor.SlimmingConfiguration;
import com.alipay.sofa.koupleless.automoduleconvertor.PomModifier;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.function.Consumer;

public class Launcher {
    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);
    private static Scanner      scanner;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        try {
            logger.info("开始执行主程序");

            String projectPath = getValidInput("请输入工程的绝对路径：", Launcher::validateProjectPath);
            String applicationName = getValidInput("请输入要设置的应用名称：",
                Launcher::validateApplicationName);
            executeOperation("修改 application.properties",
                () -> modifyApplicationProperties(projectPath, applicationName));
            executeOperation("创建 bootstrap.properties",
                () -> createBootstrapProperties(projectPath));
            executeOperation("修改 pom.xml", () -> modifyPomXml(projectPath, applicationName));

            logger.info("所有操作已完成");

        } catch (Exception e) {
            logger.error("发生错误: {}", e.getMessage());
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    private static String getValidInput(String prompt, Consumer<String> validator) {
        while (true) {
            logger.info(prompt);
            String input = scanner.nextLine().trim();
            try {
                validator.accept(input);
                return input;
            } catch (IllegalArgumentException e) {
                logger.warn(e.getMessage());
            }
        }
    }

    private static void validateProjectPath(String projectPath) {
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            throw new IllegalArgumentException("提供的项目路径不存在或不是目录");
        }
    }

    private static void validateApplicationName(String applicationName) {
        if (applicationName.isEmpty()) {
            throw new IllegalArgumentException("应用名称不能为空");
        }
    }

    private static void executeOperation(String operationName, OperationExecutor operation) {
        logger.info("开始{}", operationName);
        try {
            operation.execute();
            logger.info("{}完成", operationName);
        } catch (Exception e) {
            logger.error("{}时发生错误: {}", operationName, e.getMessage());
        }
    }

    private static void modifyApplicationProperties(String projectPath,
                                                    String applicationName) throws IOException {
        ApplicationPropertiesModifier.modifyApplicationProperties(projectPath, applicationName);
    }

    private static void createBootstrapProperties(String projectPath) {
        Path arkPath = Paths.get(projectPath, "conf", "ark");
        SlimmingConfiguration.createBootstrapProperties(arkPath.toString(), "bootstrap.properties");
    }

    private static void modifyPomXml(String projectPath, String applicationName) throws IOException,
                                                                                 JDOMException {
        PomModifier.processProjectPath(projectPath, applicationName);
    }

    @FunctionalInterface
    private interface OperationExecutor {
        void execute() throws Exception;
    }
}

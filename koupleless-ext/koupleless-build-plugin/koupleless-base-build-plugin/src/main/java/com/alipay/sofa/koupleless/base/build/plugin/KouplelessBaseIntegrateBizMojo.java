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
package com.alipay.sofa.koupleless.base.build.plugin;

import com.alipay.sofa.koupleless.base.build.plugin.common.JarFileUtils;
import com.alipay.sofa.koupleless.base.build.plugin.model.ArkConfigHolder;
import com.alipay.sofa.koupleless.base.build.plugin.model.KouplelessIntegrateBizConfig;
import com.alipay.sofa.koupleless.base.build.plugin.utils.ParseUtils;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.SOFA_ARK_MODULE;
import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.EXTENSION_INTEGRATE_LOCAL_DIRS;
import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.EXTENSION_INTEGRATE_LOCAL_URLS;
import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.FILE_PREFIX;
import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.HTTPS_PREFIX;
import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.HTTP_PREFIX;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: KouplelessBaseIntegrateBizMojo.java, v 0.1 2024年06月25日 11:59 立蓬 Exp $
 */
@Mojo(name = "integrate-biz", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class KouplelessBaseIntegrateBizMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}", required = true)
    private File                 baseDir;

    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    private File                 outputDirectory;

    KouplelessIntegrateBizConfig kouplelessIntegrateBizConfig = new KouplelessIntegrateBizConfig();

    /** {@inheritDoc} */
    @Override
    public void execute() {
        try {
            initKouplelessIntegrateBizConfig();
            integrateBizToResource();
        } catch (Throwable t) {
            getLog().error(t);
            throw new RuntimeException(t);
        }
    }

    private void integrateBizToResource() throws IOException {
        copyFilesToResource();
        copyLocalDirsToResource();
    }

    private void copyFilesToResource() throws IOException {
        File targetDir = new File(outputDirectory, SOFA_ARK_MODULE);
        for (String urlStr : kouplelessIntegrateBizConfig.getFileURLs()) {
            File targetFile = new File(targetDir, getFullRevision(urlStr));
            if (!targetFile.exists()) {
                URL url = new URL(urlStr);
                try (InputStream inputStream = url.openStream()) {
                    FileUtils.copyInputStreamToFile(inputStream, targetFile);
                }
            }
        }
    }

    private String getFullRevision(String bizUrl) {
        // file:///xxx/{name}-{version}-ark-biz.jar
        if (bizUrl.startsWith(FILE_PREFIX)) {
            return StringUtils.substringAfterLast(bizUrl, File.separator);
        }

        // https://xxx/{name}-{version}.jar
        if (bizUrl.startsWith(HTTP_PREFIX) || bizUrl.startsWith(HTTPS_PREFIX)) {
            return StringUtils.substringAfterLast(bizUrl, "/");
        }

        return "unknownBizName-unknownBizVersion.jar";
    }

    private void copyLocalDirsToResource() throws IOException {
        File targetDir = new File(outputDirectory, SOFA_ARK_MODULE);
        for (String dirPath : kouplelessIntegrateBizConfig.getLocalDirURLs()) {
            for (File bizSourceFile : getBizFileFromLocalFileSystem(dirPath)) {
                File targetFile = new File(targetDir, bizSourceFile.getName());
                if (!targetFile.exists()) {
                    FileUtils.copyFileToDirectory(bizSourceFile, targetDir);
                }
            }
        }
    }

    @SneakyThrows
    public List<File> getBizFileFromLocalFileSystem(String absoluteBizDirPath) {
        List<File> bizFiles = new ArrayList<>();
        Files.walkFileTree(new File(absoluteBizDirPath).toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                Path absolutePath = file.toAbsolutePath();
                if (absolutePath.toString().endsWith(".jar")) {
                    Map<String, Object> attributes = JarFileUtils
                        .getMainAttributes(absolutePath.toString());
                    if (attributes.containsKey("Ark-Biz-Name")) {
                        getLog().info(String.format("Found biz jar file: %s", absolutePath));
                        bizFiles.add(file.toFile());
                    }
                }

                return FileVisitResult.CONTINUE;
            }
        });

        return bizFiles;
    }

    private void initKouplelessIntegrateBizConfig() {
        readFromArkPropertiesFile();
        readFromArkYamlFile();
    }

    private void readFromArkYamlFile() {
        Map<String, Object> arkYaml = ArkConfigHolder.getArkYaml(baseDir.getAbsolutePath());
        kouplelessIntegrateBizConfig
            .addFileURLs(ParseUtils.getStringSet(arkYaml, EXTENSION_INTEGRATE_LOCAL_URLS));
        kouplelessIntegrateBizConfig
            .addLocalDirURLs(ParseUtils.getStringSet(arkYaml, EXTENSION_INTEGRATE_LOCAL_DIRS));
    }

    private void readFromArkPropertiesFile() {
        Properties arkProperties = ArkConfigHolder.getArkProperties(baseDir.getAbsolutePath());
        kouplelessIntegrateBizConfig
            .addFileURLs(ParseUtils.getSetValues(arkProperties, EXTENSION_INTEGRATE_LOCAL_URLS));
        kouplelessIntegrateBizConfig.addLocalDirURLs(
            ParseUtils.getSetValues(arkProperties, EXTENSION_INTEGRATE_LOCAL_DIRS));

    }
}
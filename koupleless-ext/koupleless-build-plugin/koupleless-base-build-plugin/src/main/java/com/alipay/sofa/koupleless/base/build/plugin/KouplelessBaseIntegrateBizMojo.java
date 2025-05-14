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

import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.AUTHORIZATION_BASIC;
import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.EXTENSION_INTEGRATE_LOCAL_DIRS;
import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.EXTENSION_INTEGRATE_URLS;
import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.FILE_PREFIX;
import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.HTTPS_PREFIX;
import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.HTTP_PREFIX;
import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.INTEGRATE_BIZ_URL;
import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.INTEGRATE_BIZ_URL_AUTHORIZATION_TYPE;
import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.INTEGRATE_BIZ_URL_BASIC_PASSWORD;
import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.INTEGRATE_BIZ_URL_BASIC_USERNAME;
import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.SOFA_ARK_MODULE;

import com.alibaba.fastjson.JSON;
import com.alipay.sofa.koupleless.base.build.plugin.model.ArkConfigHolder;
import com.alipay.sofa.koupleless.base.build.plugin.model.KouplelessIntegrateBizConfig;
import com.alipay.sofa.koupleless.base.build.plugin.model.KouplelessIntegrateBizConfig.KouplelessIntegrateBizAuthorization;
import com.alipay.sofa.koupleless.base.build.plugin.utils.JarFileUtils;
import com.alipay.sofa.koupleless.base.build.plugin.utils.ParseUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: KouplelessBaseIntegrateBizMojo.java, v 0.1 2024年06月25日 11:59 立蓬 Exp $
 */
@Mojo(name = "integrate-biz", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class KouplelessBaseIntegrateBizMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}", required = true)
    File                         baseDir;

    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    File                         outputDirectory;

    KouplelessIntegrateBizConfig kouplelessIntegrateBizConfig = new KouplelessIntegrateBizConfig();

    /**
     * {@inheritDoc}
     */
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

    void integrateBizToResource() throws IOException {
        copyFilesToResource();
        copyLocalDirsToResource();
    }

    protected void copyFilesToResource() throws IOException {
        File targetDir = new File(outputDirectory, SOFA_ARK_MODULE);
        for (String urlStr : kouplelessIntegrateBizConfig.getFileURLs()) {
            File targetFile = new File(targetDir, getFullRevision(urlStr));
            if (!targetFile.exists()) {
                URL url = new URL(urlStr);
                URLConnection conn = url.openConnection();

                if (StringUtils.startsWith(urlStr, HTTP_PREFIX)
                    || StringUtils.startsWith(urlStr, HTTPS_PREFIX)) {
                    // 如果是http或https协议，设置认证头
                    setAuthorizationHeader(conn);
                }
                try (InputStream inputStream = conn.getInputStream()) {
                    FileUtils.copyInputStreamToFile(inputStream, targetFile);
                }
            }
        }
    }

    private void setAuthorizationHeader(URLConnection conn) throws IOException {
        KouplelessIntegrateBizAuthorization httpAuthorization = kouplelessIntegrateBizConfig
            .getHttpAuthorization();
        if (httpAuthorization == null
            || StringUtils.isBlank(httpAuthorization.getAuthorizationType())) {
            return;
        }
        switch (httpAuthorization.getAuthorizationType()) {
            case AUTHORIZATION_BASIC:
                String username = httpAuthorization.getBasicUsername();
                String password = httpAuthorization.getBasicPassword();
                if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
                    throw new IllegalArgumentException(
                        "Both username and password should be set when using basic authorization.");
                }
                String auth = username + ":" + password;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
                break;
            default:
                throw new UnsupportedOperationException(
                    "Unsupported authorization type: " + httpAuthorization.getAuthorizationType());
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

        throw new UnsupportedOperationException(String.format(
            "Unsupported protocol for %s, only support file://, http:// and https://", bizUrl));
    }

    protected void copyLocalDirsToResource() throws IOException {
        File targetDir = new File(outputDirectory, SOFA_ARK_MODULE);
        for (String dirPath : kouplelessIntegrateBizConfig.getLocalDirs()) {
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

    protected void initKouplelessIntegrateBizConfig() {
        readFromArkPropertiesFile();
        readFromArkYamlFile();
    }

    private void readFromArkYamlFile() {
        Map<String, Object> arkYaml = ArkConfigHolder.getArkYaml(baseDir.getAbsolutePath());
        kouplelessIntegrateBizConfig
            .addFileURLs(ParseUtils.getStringSet(arkYaml, EXTENSION_INTEGRATE_URLS));
        kouplelessIntegrateBizConfig
            .addLocalDirs(ParseUtils.getStringSet(arkYaml, EXTENSION_INTEGRATE_LOCAL_DIRS));
        kouplelessIntegrateBizConfig.setHttpAuthorization(
            JSON.parseObject(JSON.toJSONString(arkYaml.get(INTEGRATE_BIZ_URL)),
                KouplelessIntegrateBizAuthorization.class));
    }

    private void readFromArkPropertiesFile() {
        Properties arkProperties = ArkConfigHolder.getArkProperties(baseDir.getAbsolutePath());
        kouplelessIntegrateBizConfig
            .addFileURLs(ParseUtils.getSetValues(arkProperties, EXTENSION_INTEGRATE_URLS));
        kouplelessIntegrateBizConfig
            .addLocalDirs(ParseUtils.getSetValues(arkProperties, EXTENSION_INTEGRATE_LOCAL_DIRS));
        KouplelessIntegrateBizAuthorization httpAuthorization = new KouplelessIntegrateBizAuthorization();
        httpAuthorization
            .setAuthorizationType(arkProperties.getProperty(INTEGRATE_BIZ_URL_AUTHORIZATION_TYPE));
        httpAuthorization
            .setBasicUsername(arkProperties.getProperty(INTEGRATE_BIZ_URL_BASIC_USERNAME));
        httpAuthorization
            .setBasicPassword(arkProperties.getProperty(INTEGRATE_BIZ_URL_BASIC_PASSWORD));
        kouplelessIntegrateBizConfig.setHttpAuthorization(httpAuthorization);
    }
}

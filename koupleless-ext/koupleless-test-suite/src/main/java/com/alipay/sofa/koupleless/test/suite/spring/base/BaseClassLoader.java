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
package com.alipay.sofa.koupleless.test.suite.spring.base;

import com.alipay.sofa.ark.common.util.ClassLoaderUtils;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.common.util.OSUtils;
import com.alipay.sofa.koupleless.test.suite.common.IntegrationLogger;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.apache.commons.collections4.iterators.IteratorEnumeration;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;

/**
 * @author CodeNoobKing
 * @since 2024/3/22
 **/
public class BaseClassLoader extends URLClassLoader {

    private ClassLoader    parent;
    private URLClassLoader higherPriorityResourceClassLoader;

    private URL[]          parentUrls;

    private List<String>   excludeArtifactIds = new ArrayList<>();

    @SneakyThrows
    public static List<URL> getUrlsFromSurefireManifest(URL url) {
        List<URL> urls = Lists.newArrayList();
        String parentPath = new File(url.getPath()).getParent();
        try (JarFile jarFile = new JarFile(url.getFile())) {
            String classPathValue = jarFile.getManifest().getMainAttributes()
                .getValue("Class-Path");
            if (StringUtils.isEmpty(classPathValue)) {
                return urls;
            }
            String[] classPaths = classPathValue.split(" ");
            for (String classFilePath : classPaths) {
                String filePath = parseFilePath(classFilePath, parentPath);
                urls.add(new URL(filePath));
            }
        }
        return urls;
    }

    private static String parseFilePath(String classFilePath, String parentPath) {
        String fileUrlPrefix = "file:";
        // 需要添加 file: 前缀
        String filePath = StringUtils.startsWith(classFilePath, fileUrlPrefix) ? classFilePath
            : OSUtils.getLocalFileProtocolPrefix() + new File(parentPath, classFilePath).getPath();

        // 如果是文件夹(即：以/结尾)，则需要添加/
        filePath = (classFilePath.endsWith("/") && !filePath.endsWith("/")) ? filePath + "/"
            : filePath;

        return filePath;
    }

    @SneakyThrows
    public static List<URL> getUrls(ClassLoader classLoader) {
        List<URL> urls = Lists.newArrayList();
        for (URL url : ClassLoaderUtils.getURLs(classLoader)) {
            urls.add(url);

            if (url.toString().matches(".*target/surefire.*")) {
                List<URL> urlsFromSurefireManifest = getUrlsFromSurefireManifest(url);
                ArkletLoggerFactory.getDefaultLogger().info("{}, urlsFromSurefireManifest",
                    urlsFromSurefireManifest);
                urls.addAll(urlsFromSurefireManifest);
            }
        }

        return urls;
    }

    public BaseClassLoader(ClassLoader parent, String baseArtifactId,
                           List<String> higherPriorityArtifacts, List<String> excludeArtifactIds) {
        // add an interception layer to the parent classloader
        // in this way we can control the classloading process
        super(new URL[0], parent);
        this.parent = parent;
        this.excludeArtifactIds = excludeArtifactIds;
        this.excludeArtifactIds.remove(baseArtifactId);

        List<URL> urls = Lists.newArrayList();
        this.parentUrls = getUrls(this.parent).toArray(new URL[0]);
        for (URL url : parentUrls) {
            if (higherPriorityArtifacts.stream().anyMatch(url.toString()::contains)) {
                urls.add(url);
            }
        }
        this.higherPriorityResourceClassLoader = new URLClassLoader(urls.toArray(new URL[0]), null);
        IntegrationLogger.getLogger().debug("{}, ParentUrls", parentUrls);
        IntegrationLogger.getLogger().debug("{}, BaseArtifactId", baseArtifactId);
        IntegrationLogger.getLogger().debug("{}, ExcludeArtifactIds", excludeArtifactIds);
    }

    @Override
    public URL[] getURLs() {
        return Arrays.stream(parentUrls)
            .filter(url -> !excludeArtifactIds.stream().anyMatch(url.toString()::contains))
            .toArray(URL[]::new);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> higherPriorityUrls = higherPriorityResourceClassLoader.getResources(name);
        Enumeration<URL> originalResources = super.getResources(name);
        List<URL> result = new ArrayList<>();

        while (higherPriorityUrls != null && higherPriorityUrls.hasMoreElements()) {
            result.add(higherPriorityUrls.nextElement());
        }

        while (originalResources.hasMoreElements()) {
            URL resource = originalResources.nextElement();
            if (!excludeArtifactIds.stream().anyMatch(resource.toString()::contains)
                && !result.contains(resource)) {
                result.add(resource);
            }
        }

        return new IteratorEnumeration<>(result.iterator());
    }

    @Override
    public URL getResource(String name) {
        try {
            Enumeration<URL> urlEnumeration = getResources(name);
            // to List
            List<URL> urls = new ArrayList<>();
            while (urlEnumeration.hasMoreElements()) {
                urls.add(urlEnumeration.nextElement());
            }
            // if there are multiple resources, return the first one
            if (urls.size() > 0) {
                return urls.get(0);
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BaseClassLoader) {
            return ((BaseClassLoader) obj).parent.equals(parent);
        }
        return parent.equals(obj);
    }
}

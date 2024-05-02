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

import com.alipay.sofa.koupleless.common.util.OSUtils;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.List;
import java.util.jar.JarFile;

/**
 * <p>BaseClassLoader class.</p>
 *
 * @author CodeNoobKing
 * @since 2024/3/22
 * @version 1.0.0
 */
public class BaseClassLoader extends URLClassLoader {

    private URLClassLoader parent;
    private URLClassLoader higherPriorityClassLoader;

    private URL[]          parentUrls;

    /**
     * <p>getUrlsFromSurefireManifest.</p>
     *
     * @param url a {@link java.net.URL} object
     * @return a {@link java.util.List} object
     */
    @SneakyThrows
    public static List<URL> getUrlsFromSurefireManifest(URL url) {
        List<URL> urls = Lists.newArrayList();
        String parentPath = Paths.get(url.getFile()).getParent().toString();
        String fileUrlPrefix = "file:";
        try (JarFile jarFile = new JarFile(url.getFile())) {
            String classPathValue = jarFile.getManifest().getMainAttributes()
                .getValue("Class-Path");
            String[] classPaths = classPathValue.split(" ");
            for (String classFilePath : classPaths) {
                String filePath = StringUtils.startsWith(classFilePath, fileUrlPrefix)
                    ? classFilePath
                    : OSUtils.getLocalFileProtocolPrefix() + Paths.get(parentPath, classFilePath);
                urls.add(new URL(filePath));
            }
        }
        return urls;
    }

    /**
     * <p>getUrls.</p>
     *
     * @param classLoader a {@link java.net.URLClassLoader} object
     * @return a {@link java.util.List} object
     */
    @SneakyThrows
    public static List<URL> getUrls(URLClassLoader classLoader) {
        List<URL> urls = Lists.newArrayList();
        for (URL url : classLoader.getURLs()) {
            urls.add(url);

            if (url.toString().matches(".*target/surefire.*")) {
                urls.addAll(getUrlsFromSurefireManifest(url));
            }
        }

        return urls;
    }

    /**
     * <p>Constructor for BaseClassLoader.</p>
     *
     * @param higherPriorityArtifacts a {@link java.util.List} object
     * @param parent a {@link java.lang.ClassLoader} object
     */
    public BaseClassLoader(List<String> higherPriorityArtifacts, ClassLoader parent) {
        // add an interception layer to the parent classloader
        // in this way we can control the classloading process
        super(new URL[0], parent);
        this.parent = (URLClassLoader) parent;

        List<URL> urls = Lists.newArrayList();
        this.parentUrls = getUrls(this.parent).toArray(new URL[0]);
        for (URL url : parentUrls) {
            if (higherPriorityArtifacts.stream().anyMatch(url.toString()::contains)) {
                urls.add(url);
            }
        }
        this.higherPriorityClassLoader = new URLClassLoader(urls.toArray(new URL[0]));
    }

    /** {@inheritDoc} */
    @Override
    public URL[] getURLs() {
        return parentUrls;
    }

    /** {@inheritDoc} */
    @Override
    public URL getResource(String name) {
        URL resource = higherPriorityClassLoader.getResource(name);
        resource = resource != null ? resource : super.getResource(name);
        return resource;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj.equals(this)) {
            return true;
        }
        return parent.equals(obj);
    }
}

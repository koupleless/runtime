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
package com.alipay.sofa.koupleless.test.suite.biz;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderService;
import com.alipay.sofa.ark.support.common.DelegateToMasterBizClassLoaderHook;
import com.google.common.collect.Maps;
import edu.emory.mathcs.backport.java.util.Collections;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * <p>TestClassLoaderHook class.</p>
 *
 * @author CodeNoobKing
 * @since 2024/3/21
 * @version 1.0.0
 */
public class TestClassLoaderHook extends DelegateToMasterBizClassLoaderHook {

    @Setter
    private static String                       buildDirectory                  = "target/classes";

    private ConcurrentMap<String, List<String>> higherPriorityResourceArtifacts = Maps
        .newConcurrentMap();

    /**
     * <p>putHigherPriorityResourceArtifacts.</p>
     *
     * @param identity a {@link java.lang.String} object
     * @param artifacts a {@link java.util.List} object
     */
    public void putHigherPriorityResourceArtifacts(String identity, List<String> artifacts) {
        higherPriorityResourceArtifacts.put(identity, artifacts);
    }

    private URL findResourceInJarFile(File file, String resource) throws Throwable {
        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (StringUtils.equals(entry.getName(), resource)) {
                    return new URL("jar:file:" + file.getAbsolutePath() + "!/" + entry.getName());
                }
            }
            return null;
        }
    }

    private URL findResourceInBuildDirectory(File file, String resource) throws Throwable {
        File resourceFile = new File(file, resource);
        if (resourceFile.exists()) {
            return resourceFile.toURI().toURL();
        }
        return null;
    }

    /** {@inheritDoc} */
    @SneakyThrows
    @Override
    public URL preFindResource(String name, ClassLoaderService classLoaderService, Biz biz) {
        String identity = biz.getIdentity();
        List<String> artifacts = higherPriorityResourceArtifacts.getOrDefault(identity,
            Collections.emptyList());

        URL targetUrl = null;
        for (String artifact : artifacts) {
            ClassLoader baseClassLoader = classLoaderService.getMasterBizClassLoader();
            if (baseClassLoader instanceof URLClassLoader) {
                URLClassLoader urlClassLoader = (URLClassLoader) baseClassLoader;
                for (URL url : urlClassLoader.getURLs()) {
                    // located in a jar file
                    if (url.toString().endsWith(".jar") && url.toString().contains(artifact)) {
                        targetUrl = findResourceInJarFile(new File(url.getFile()), name);

                    }

                    // located in a build directory
                    if (url.toString().contains(buildDirectory)
                        && url.toString().contains(artifact)) {
                        targetUrl = findResourceInBuildDirectory(new File(url.getFile()), name);
                    }

                    if (targetUrl != null) {
                        return targetUrl;
                    }
                }
            }
        }

        return super.preFindResource(name, classLoaderService, biz);
    }
}

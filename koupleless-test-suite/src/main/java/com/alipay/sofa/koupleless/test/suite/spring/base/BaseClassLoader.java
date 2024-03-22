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

import com.google.common.collect.Lists;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * @author CodeNoobKing
 * @date 2024/3/22
 **/
public class BaseClassLoader extends URLClassLoader {

    private URLClassLoader parent;
    private URLClassLoader higherPriorityClassLoader;

    public BaseClassLoader(List<String> higherPriorityArtifacts, ClassLoader parent) {
        // add an interception layer to the parent classloader
        // in this way we can control the classloading process
        super(new URL[0], parent);
        this.parent = (URLClassLoader) parent;

        List<URL> urls = Lists.newArrayList();
        for (URL url : this.parent.getURLs()) {
            if (higherPriorityArtifacts.stream().anyMatch(url.toString()::contains)) {
                urls.add(url);
            }
        }
        this.higherPriorityClassLoader = new URLClassLoader(urls.toArray(new URL[0]));
    }

    @Override
    public URL[] getURLs() {
        return super.getURLs();
    }

    @Override
    public URL getResource(String name) {
        URL resource = higherPriorityClassLoader.getResource(name);
        resource = resource != null ? resource : super.getResource(name);
        return resource;
    }
}

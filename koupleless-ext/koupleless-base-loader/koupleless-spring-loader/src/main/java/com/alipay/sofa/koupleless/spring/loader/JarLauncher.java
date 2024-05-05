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
package com.alipay.sofa.koupleless.spring.loader;

/**
 *
 * @author syd
 * @version JarLauncher.java, v 0.1 2023年12月26日 14:54 syd
 */
import java.net.URL;

/**
 * A JarLauncher to load classes with CachedLaunchedURLClassLoader
 *
 * @author zjulbj
 * @author bingjie.lbj
 * @since 2023/12/26
 */
public class JarLauncher extends org.springframework.boot.loader.JarLauncher {
    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects
     * @throws java.lang.Exception if any.
     */
    public static void main(String[] args) throws Exception {
        new JarLauncher().launch(args);
    }

    /** {@inheritDoc} */
    @Override
    protected ClassLoader createClassLoader(URL[] urls) throws Exception {
        return new CachedLaunchedURLClassLoader(isExploded(), getArchive(), urls,
            getClass().getClassLoader());
    }
}

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

import com.alipay.sofa.ark.container.service.classloader.BizClassLoader;
import com.alipay.sofa.ark.exception.ArkLoaderException;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * <p>TestBizClassLoader class.</p>
 *
 * @author zzl_i
 * @version 1.0.0
 */
public class TestBizClassLoader extends BizClassLoader {

    private List<Object> resolveByClassLoaderPatterns;

    private List<String> includedArtifactIds = new ArrayList<>();

    @SneakyThrows
    private void initHook() {
        Field bizClassLoaderHookField = BizClassLoader.class.getDeclaredField("bizClassLoaderHook");
        bizClassLoaderHookField.setAccessible(true);
        bizClassLoaderHookField.set(this, TestBootstrap.getClassLoaderHook());
        bizClassLoaderHookField.setAccessible(false);

        Field bizClassLoaderIsHookLoadedField = BizClassLoader.class
            .getDeclaredField("isHookLoaded");
        bizClassLoaderIsHookLoadedField.setAccessible(true);
        bizClassLoaderIsHookLoadedField.set(this, new AtomicBoolean(true));
        bizClassLoaderIsHookLoadedField.setAccessible(false);

    }

    /**
     * <p>Constructor for TestBizClassLoader.</p>
     *
     * @param bizIdentity          a {@link java.lang.String} object
     * @param includeClassNames    a {@link java.util.List} object
     * @param includeClassPatterns a {@link java.util.List} object
     * @param baseClassLoader      a {@link java.net.URLClassLoader} object
     */
    public TestBizClassLoader(String bizIdentity, List<String> includeClassNames,
                              List<Pattern> includeClassPatterns, List<String> includeArtifactIds,
                              URLClassLoader baseClassLoader) {
        super(bizIdentity, baseClassLoader.getURLs());
        initHook();
        this.resolveByClassLoaderPatterns = new ArrayList<>();
        this.resolveByClassLoaderPatterns.addAll(includeClassPatterns);
        this.resolveByClassLoaderPatterns.addAll(includeClassNames);
        this.includedArtifactIds.addAll(includeArtifactIds);

    }

    /**
     * {@inheritDoc}
     * <p>
     * 重写 resolveLocalClass 方法，根据 resolveByClassLoaderPatterns 判断是否需要使用 baseClassLoader 加载类。
     * 这是因为，当我们做集成测试兼容的时候，我们需要区分地加载同一个包里的哪些类在 baseClassLoader 加载，哪些类在 bizClassLoader 加载。
     * 这个能力在正常的 ark 使用场景下是不具备的，因为正常 sofa-ark 是包粒度的。
     * 最典型的场景是，XXXTest 本身肯定是由 bizClassLoader 加载的，但是 XXXTest 依赖的类，是由 baseClassLoader 加载的。
     * <p>
     * 虽然我们也可以通过自定义 plugin 来控制包粒度，但是这样做的话，一方面可能会影响正常的 plugin 使用。
     * 另一方面，mock 的复杂度实在是太高了。
     * <p>
     * 故，此处采用简单的方法重写，来控制集成测试时期哪些类由 baseClassLoader 加载，哪些类由 bizClassLoader 加载。
     * 如果目标类在 resolveByClassLoaderPatterns 中，则使用 bizClassLoader 加载。
     * 如果目标类不在 resolveByClassLoaderPatterns 中，则使用 baseClassLoader 加载。
     */
    @Override
    public Class<?> resolveLocalClass(String name) {
        for (Object resolveByClassLoaderPattern : resolveByClassLoaderPatterns) {
            if (resolveByClassLoaderPattern instanceof String
                && Objects.equals(resolveByClassLoaderPattern, name)) {
                return super.resolveLocalClass(name);
            }

            if (resolveByClassLoaderPattern instanceof Pattern
                && ((Pattern) resolveByClassLoaderPattern).matcher(name).matches()) {
                Class<?> clz = super.resolveLocalClass(name);
                return clz;
            }
        }

        // default to base classLoader
        return null;
    }

    // since we cannot acquire package of clz, we have to perform a post interception
    @Override
    public Class<?> loadClassInternal(String name, boolean resolve) throws ArkLoaderException {
        Class<?> clz = super.loadClassInternal(name, resolve);

        String codeSourceLocation = Optional.ofNullable(clz.getProtectionDomain())
            .map(ProtectionDomain::getCodeSource).map(CodeSource::getLocation).map(URL::toString)
            .orElse("");

        for (String includedArtifactId : includedArtifactIds) {
            // should be load by test class loader
            if (codeSourceLocation.contains(includedArtifactId)
                && !this.equals(clz.getClassLoader())) {
                return super.resolveLocalClass(name);
            }
        }

        return clz;
    }

}

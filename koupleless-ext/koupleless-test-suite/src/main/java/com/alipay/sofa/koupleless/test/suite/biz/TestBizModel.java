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

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * SOFAArk 的测试 BIZ 类。
 *
 * @author zzl_i
 * @version 1.0.0
 */
public class TestBizModel extends BizModel {

    private TestBizConfig config;

    /**
     * <p>Constructor for TestBizModel.</p>
     *
     * @param config a {@link com.alipay.sofa.koupleless.test.suite.biz.TestBizConfig} object
     */
    public TestBizModel(TestBizConfig config) {

        super();
        this.config = config;

        List<Pattern> compiledIncludeClassPatterns = CollectionUtils
            .emptyIfNull(config.getIncludeClassPatterns()).stream().map(Pattern::compile)
            .collect(Collectors.toList());

        List<URL> pluginUrls = new ArrayList<>();
        PluginManagerService pluginManagerService = ArkClient.getPluginManagerService();
        for (Plugin plugin : pluginManagerService.getPluginsInOrder()) {
            pluginUrls.add(plugin.getPluginURL());
        }
        this.setBizName(config.getBizName()).setBizVersion(config.getBizVersion())
            .setBizState(BizState.RESOLVED).setDenyImportClasses("").setDenyImportPackages("")
            .setDenyImportPackages("").setPluginClassPath(pluginUrls.toArray(new URL[0]));
        registerBiz();

        TestBizClassLoader testBizClassLoader = new TestBizClassLoader(config.getBizName(),
            config.getTestClassNames(), compiledIncludeClassPatterns,
            config.getIncludeArtifactIds(), config.getBaseClassLoader());
        testBizClassLoader.setBizModel(this);
        testBizClassLoader.setBizIdentity(this.getIdentity());
        this.setClassLoader(testBizClassLoader);
    }

    /**
     * <p>registerBiz.</p>
     */
    public void registerBiz() {
        // firstly, we need to register the biz into ark container.
        ArkClient.getBizManagerService().registerBiz(this);
    }

    /**
     * {@inheritDoc}
     *
     * due to the complexity of mock a BizClassLoader environment, we just return true here.
     * we should control the class loading behaviour by other more controlled way.
     */
    @Override
    public boolean isDeclared(URL url, String resourceName) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDeclaredMode() {
        return true;
    }

    /**
     * <p>getTestClasses.</p>
     *
     * @return a {@link java.util.List} object
     */
    @SneakyThrows
    public List<Class<?>> getTestClasses() {
        List<Class<?>> classInBizLoader = new ArrayList<>();
        for (String testClassName : config.getTestClassNames()) {
            classInBizLoader.add(getBizClassLoader().loadClass(testClassName));
        }
        return classInBizLoader;
    }

    /**
     * <p>executeTest.</p>
     *
     * @param runnable a {@link java.lang.Runnable} object
     * @return a {@link java.util.concurrent.CompletableFuture} object
     */
    @SneakyThrows
    public CompletableFuture<Void> executeTest(Runnable runnable) {
        if (StringUtils.isNoneBlank(config.getBootstrapClassName())) {
            // bootstrap the biz, do some initialization in side base.
            Class<?> bootstrapClass = config.getBaseClassLoader()
                .loadClass(config.getBootstrapClassName());
            Method bootstrapBaseMethod = bootstrapClass.getMethod("bootstrapBase");
            bootstrapBaseMethod.invoke(bootstrapClass.newInstance());
        }

        return CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setContextClassLoader(getBizClassLoader());
                // because there is no thread pool, so we dont need to concern classLoader switching.
                // just update the new classLoader to current thread.
                runnable.run();
            }
        }, command -> new Thread(command).start());
    }
}

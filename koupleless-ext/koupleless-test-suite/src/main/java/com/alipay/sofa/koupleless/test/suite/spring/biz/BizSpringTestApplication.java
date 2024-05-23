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
package com.alipay.sofa.koupleless.test.suite.spring.biz;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.common.util.ClassLoaderUtils;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStartupEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizStartupEvent;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.koupleless.test.suite.biz.TestBizConfig;
import com.alipay.sofa.koupleless.test.suite.biz.TestBizModel;
import com.alipay.sofa.koupleless.test.suite.spring.framwork.SpringTestUtils;
import com.alipay.sofa.koupleless.test.suite.spring.model.BizSpringTestConfig;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * <p>BizSpringTestApplication class.</p>
 *
 * @author CodeNoobKing
 * @since 2024/3/6
 * @version 1.0.0
 */
@Getter
public class BizSpringTestApplication {

    private TestBizModel                   testBiz;

    private ConfigurableApplicationContext applicationContext;

    private BizSpringTestConfig            config;

    @SneakyThrows
    /**
     * <p>Constructor for BizSpringTestApplication.</p>
     *
     * @param config a {@link com.alipay.sofa.koupleless.test.suite.spring.model.BizSpringTestConfig} object
     */
    public BizSpringTestApplication(BizSpringTestConfig config) {
        config.init();
        this.config = config;
    }

    /**
     * <p>isExcludedDependency.</p>
     *
     * @param dependency a {@link java.lang.String} object
     * @return a boolean
     */
    public boolean isExcludedDependency(String dependency) {
        for (String regexp : CollectionUtils
            .emptyIfNull(SpringTestUtils.getConfig().getBiz().getExcludeDependencyRegexps())) {
            if (dependency.matches(".*" + regexp + ".*")) {
                return true;
            }
        }

        for (String excludePackage : CollectionUtils.emptyIfNull(config.getExcludePackages())) {
            if (dependency.matches(".*" + excludePackage + ".*")) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>initBiz.</p>
     */
    public void initBiz() {
        List<String> includeClassPatterns = config.getPackageNames().stream().map(s -> s + ".*")
            .collect(Collectors.toList());

        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        List<URL> excludedUrls = new ArrayList<>();
        for (URL url : ClassLoaderUtils.getURLs(tccl)) {
            if (!isExcludedDependency(url.toString())) {
                excludedUrls.add(url);
            }
        }

        testBiz = new TestBizModel(TestBizConfig.builder().bootstrapClassName("")
            .bizName(config.getBizName()).bizVersion("TEST").testClassNames(new ArrayList<>())
            .includeClassPatterns(includeClassPatterns)
            .baseClassLoader(new URLClassLoader(excludedUrls.toArray(new URL[0]), tccl.getParent()))
            .build());
        testBiz.setWebContextPath(config.getBizName());
    }

    /**
     * <p>run.</p>
     */
    @SneakyThrows
    public void run() {
        CompletableFuture.runAsync(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                Thread.currentThread().setContextClassLoader(testBiz.getBizClassLoader());
                EventAdminService eventAdminService = ArkClient.getEventAdminService();
                eventAdminService.sendEvent(new BeforeBizStartupEvent(testBiz));
                Class<?> mainClass = testBiz.getBizClassLoader()
                    .loadClass(config.getMainClassName());
                SpringApplication springApplication = new SpringApplication(mainClass);
                springApplication.setAdditionalProfiles(config.getBizName());
                applicationContext = springApplication.run();
                eventAdminService.sendEvent(new AfterBizStartupEvent(testBiz));

            }
        }, new Executor() {
            @Override
            public void execute(Runnable command) {
                new Thread(command).start();
            }
        }).get();
    }

}

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
package com.alipay.sofa.koupleless.test.suite.spring.multi;

/**
 * @author CodeNoobKing
 * @since 2024/3/11
 */

import com.alipay.sofa.koupleless.test.suite.biz.TestBootstrap;
import com.alipay.sofa.koupleless.test.suite.spring.base.BaseSpringTestApplication;
import com.alipay.sofa.koupleless.test.suite.spring.biz.BizSpringTestApplication;
import com.alipay.sofa.koupleless.test.suite.spring.model.BizSpringTestConfig;
import com.alipay.sofa.koupleless.test.suite.spring.model.MultiSpringTestConfig;
import lombok.Getter;

import java.util.*;

/**
 * <p>TestMultiSpringApplication class.</p>
 *
 * @author CodeNoobKing
 * @author CodeNoobKing
 * @author CodeNoobKing
 * @author CodeNoobKing
 * @author CodeNoobKing
 * @author CodeNoobKing
 * @since 2024/3/7
 * @version 1.0.0
 */
public class TestMultiSpringApplication {

    @Getter
    private BaseSpringTestApplication             baseApplication;

    private Map<String, BizSpringTestApplication> bizApplications = new HashMap<>();

    /**
     * <p>getBizApplication.</p>
     *
     * @param bizName a {@link java.lang.String} object
     * @return a {@link com.alipay.sofa.koupleless.test.suite.spring.biz.BizSpringTestApplication} object
     */
    public BizSpringTestApplication getBizApplication(String bizName) {
        return bizApplications.get(bizName);
    }

    /**
     * <p>Constructor for TestMultiSpringApplication.</p>
     *
     * @param config a {@link com.alipay.sofa.koupleless.test.suite.spring.model.MultiSpringTestConfig} object
     */
    public TestMultiSpringApplication(MultiSpringTestConfig config) {
        this.baseApplication = new BaseSpringTestApplication(config.getBaseConfig());
        for (BizSpringTestConfig bizConfig : config.getBizConfigs()) {
            this.bizApplications.put(bizConfig.getBizName(),
                new BizSpringTestApplication(bizConfig));
        }
    }

    /**
     * <p>runBase.</p>
     */
    public void runBase() {
        baseApplication.run();
    }

    /**
     * <p>runBiz.</p>
     *
     * @param bizName a {@link java.lang.String} object
     */
    public void runBiz(String bizName) {
        bizApplications.get(bizName).initBiz(); // register biz to ark container
        bizApplications.get(bizName).run(); // run biz
    }

    /**
     * <p>bootStrapBase.</p>
     */
    public void bootStrapBase() {
        baseApplication.initBaseClassLoader();
    }

    /**
     * <p>bootStrapTest.</p>
     */
    public void bootStrapTest() {
        TestBootstrap.init(baseApplication.getBaseClassLoader());
        for (Map.Entry<String, BizSpringTestApplication> entry : bizApplications.entrySet()) {
            BizSpringTestApplication app = entry.getValue();
            String bizIdentity = app.getConfig().getBizName() + ":TEST";
            String artifactId = app.getConfig().getArtifactId();
            List<String> artifacts = Arrays.asList(artifactId, app.getConfig().getBizName());
            TestBootstrap.getClassLoaderHook().putHigherPriorityResourceArtifacts(bizIdentity,
                artifacts);
        }
    }

    /**
     * <p>run.</p>
     */
    public void run() {
        bootStrapBase();
        bootStrapTest();
        runBase();
        bizApplications.keySet().forEach(this::runBiz);
    }
}

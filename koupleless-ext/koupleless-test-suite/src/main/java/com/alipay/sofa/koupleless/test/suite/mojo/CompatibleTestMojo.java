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
package com.alipay.sofa.koupleless.test.suite.mojo;

import com.alipay.sofa.koupleless.test.suite.biz.TestBizConfig;
import com.alipay.sofa.koupleless.test.suite.biz.TestBootstrap;
import com.alipay.sofa.koupleless.test.suite.biz.TestBizModel;
import com.alipay.sofa.koupleless.test.suite.model.CompatibleTestBizConfig;
import com.alipay.sofa.koupleless.test.suite.model.CompatibleTestConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

/**
 * <p>CompatibleTestMojo class.</p>
 *
 * @author CodeNoobKing
 * @since 2024/1/15
 * @version 1.0.0
 */
@Mojo(name = "compatible-test", defaultPhase = LifecyclePhase.INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.COMPILE)
public class CompatibleTestMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    MavenProject         project;

    @Parameter(property = "compatibleTestConfigFile", defaultValue = "sofa-ark-compatible-test-config.yaml")
    String               compatibleTestConfigFile = "sofa-ark-compatible-test-config.yaml";

    private ObjectMapper yamlObjectMapper         = new ObjectMapper(new YAMLFactory())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * <p>buildURLClassLoader.</p>
     *
     * @return a {@link java.net.URLClassLoader} object
     */
    @SneakyThrows
    public URLClassLoader buildURLClassLoader() {
        List<URL> urls = new ArrayList<>();
        urls.add(new File(project.getBuild().getTestOutputDirectory()).toURI().toURL());
        urls.add(new File(project.getBuild().getOutputDirectory()).toURI().toURL());
        for (Artifact artifact : project.getArtifacts()) {
            urls.add(artifact.getFile().toURI().toURL());
        }

        for (URL url : urls) {
            getLog().debug(String.format("%s, BaseClassLoaderUrl", url));
        }

        return new URLClassLoader(urls.toArray(new URL[0]),
            // this is necessary because we are calling test engine programmatically.
            // some classes are required to be loaded by TCCL even when we are setting TCCL to biz classLoader.
            Thread.currentThread().getContextClassLoader());
    }

    @SneakyThrows
    private CompatibleTestConfig loadConfigs() {
        return yamlObjectMapper.readValue(
            Paths.get(project.getBuild().getTestOutputDirectory(), compatibleTestConfigFile).toUri()
                .toURL(),
            new TypeReference<CompatibleTestConfig>() {
            });
    }

    private List<TestBizModel> buildTestBiz(URLClassLoader baseClassLoader) {
        CompatibleTestConfig configs = loadConfigs();

        // if root project classes is not configured to include by class loader
        // then it mused be loaded by base classloader
        List<String> rootProjectClasses = new ArrayList<>();
        rootProjectClasses
            .add(Paths.get(project.getBuild().getOutputDirectory()).toAbsolutePath().toString());

        rootProjectClasses.add(
            Paths.get(project.getBuild().getTestOutputDirectory()).toAbsolutePath().toString());

        List<TestBizModel> result = new ArrayList<>();
        for (CompatibleTestBizConfig config : CollectionUtils
            .emptyIfNull(configs.getTestBizConfigs())) {

            TestBizModel testBiz = new TestBizModel(
                TestBizConfig.builder().bootstrapClassName(config.getBootstrapClass())
                    .bizName(config.getName()).bizVersion(project.getVersion())
                    .testClassNames(ListUtils.emptyIfNull(config.getTestClasses()))
                    .includeClassPatterns(ListUtils.union(
                        ListUtils.emptyIfNull(config.getLoadByBizClassLoaderPatterns()),
                        rootProjectClasses))
                    .baseClassLoader(baseClassLoader).build());

            result.add(testBiz);
        }
        return result;
    }

    /**
     * <p>executeJunit4.</p>
     */
    @SneakyThrows
    public void executeJunit4() {
        URLClassLoader baseClassLoader = buildURLClassLoader();
        TestBootstrap.init(baseClassLoader);
        TestBootstrap.registerMasterBiz();
        List<TestBizModel> sofaArkTestBizs = buildTestBiz(baseClassLoader);
        for (TestBizModel sofaArkTestBiz : sofaArkTestBizs) {
            getLog().info(String.format("%s, CompatibleTestStarted", sofaArkTestBiz.getIdentity()));

            sofaArkTestBiz.executeTest(new Runnable() {
                @Override
                @SneakyThrows
                public void run() {
                    List<Class<?>> testClasses = sofaArkTestBiz.getTestClasses();
                    Result result = JUnitCore.runClasses(testClasses.toArray(new Class[0]));
                    getLog().info(
                        String.format("%s, CompatibleTestFinished", sofaArkTestBiz.getIdentity()));

                    Preconditions.checkState(result.wasSuccessful(),
                        "Test failed: " + result.getFailures());
                }
            }).get();
        }
    }

    /**
     * <p>execute.</p>
     */
    public void execute() {
        executeJunit4();
    }
}

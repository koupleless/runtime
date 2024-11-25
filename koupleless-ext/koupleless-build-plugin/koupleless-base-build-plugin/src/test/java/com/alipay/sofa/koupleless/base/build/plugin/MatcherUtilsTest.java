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
package com.alipay.sofa.koupleless.base.build.plugin;

import com.alipay.sofa.koupleless.base.build.plugin.model.KouplelessAdapterConfig;
import com.alipay.sofa.koupleless.base.build.plugin.model.MavenDependencyAdapterMapping;
import com.alipay.sofa.koupleless.base.build.plugin.model.MavenDependencyMatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.eclipse.aether.version.InvalidVersionSpecificationException;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: MatcherUtils.java, v 0.1 2024年11月21日 10:56 立蓬 Exp $
 */
public class MatcherUtilsTest {

    String                                    MAPPING_FILE            = "adapter-mapping-test.yaml";

    private KouplelessBaseBuildPrePackageMojo mojo                    = new KouplelessBaseBuildPrePackageMojo();

    KouplelessAdapterConfig                   kouplelessAdapterConfig = loadConfig();

    Collection<MavenDependencyAdapterMapping> adapterMappings         = CollectionUtils
        .emptyIfNull(kouplelessAdapterConfig.getAdapterMappings());

    public MatcherUtilsTest() throws IOException {
    }

    private KouplelessAdapterConfig loadConfig() throws IOException {
        InputStream mappingConfigIS = this.getClass().getClassLoader()
            .getResourceAsStream(MAPPING_FILE);

        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        return yamlMapper.readValue(mappingConfigIS, KouplelessAdapterConfig.class);
    }

    /**
     * test for adaptor: koupleless-adapter-spring-boot-logback-2.7.14
     * pattern:
     *      matcher:
     *       groupId: org.springframework.boot
     *       artifactId: spring-boot
     *       versionRange: "[2.5.1,2.7.14]"
     *     adapter:
     *       artifactId: koupleless-adapter-spring-boot-logback-2.7.14
     */
    @Test
    public void testMatcher1() throws InvalidVersionSpecificationException {
        List<Dependency> res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot", "2.5.0"));
        assertEquals(0, res.size());

        res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot", "2.5.1"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-spring-boot-logback-2.7.14");

        res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot", "2.6.5"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-spring-boot-logback-2.7.14");

        res = getMatcherAdaptor(
                mockArtifact("org.springframework.boot", "spring-boot", "2.7.14"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-spring-boot-logback-2.7.14");

        res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot", "2.7.15"));
        assertEquals(0, res.size());
    }

    /**
     * test for adaptor: koupleless-adapter-logback
     * pattern:
     *     matcher:
     *       groupId: org.springframework.boot
     *       artifactId: spring-boot-starter-logging
     *       versionRange: "[1.*,)"
     *     adapter:
     *       artifactId: koupleless-adapter-logback
     */
    @Test
    public void testMatcher2() throws InvalidVersionSpecificationException {
        List<Dependency> res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot-starter-logging", "1.0.0.RELEASE"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-logback");

        res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot-starter-logging", "2.0.0.M5"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-logback");

        res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot-starter-logging", "3.3.5"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-logback");
    }

    /**
     * test for adaptor: koupleless-adapter-spring-aop-6.0.8
     * pattern:
     *     matcher:
     *       groupId: org.springframework
     *       artifactId: spring-aop
     *       versionRange: "[6.0.8,6.0.9]"
     *     adapter:
     *       artifactId: koupleless-adapter-spring-aop-6.0.8
     */
    @Test
    public void testMatcher3() throws InvalidVersionSpecificationException {
        List<Dependency> res = getMatcherAdaptor(mockArtifact("org.springframework", "org.springframework", "6.0.7"));
        assertEquals(0, res.size());

        res = getMatcherAdaptor(mockArtifact("org.springframework", "spring-aop", "6.0.8"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-spring-aop-6.0.8");

        res = getMatcherAdaptor(mockArtifact("org.springframework", "spring-aop", "6.0.9"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-spring-aop-6.0.8");

        res = getMatcherAdaptor(mockArtifact("org.springframework", "spring-aop", "6.0.10"));
        assertEquals(0, res.size());
    }

    /**
     * test for adaptor: koupleless-adapter-spring-aop-5.3.27
     * pattern:
     *     matcher:
     *       groupId: org.springframework
     *       artifactId: spring-aop
     *       versionRange: "[5.3.27,5.3.27]"
     *     adapter:
     *       artifactId: koupleless-adapter-spring-aop-5.3.27
     */
    @Test
    public void testMatcher4() throws InvalidVersionSpecificationException {
        List<Dependency> res = getMatcherAdaptor(mockArtifact("org.springframework", "spring-aop", "5.3.26"));
        assertEquals(0, res.size());

        res =  getMatcherAdaptor(mockArtifact("org.springframework", "spring-aop", "5.3.27"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-spring-aop-5.3.27");

        res =  getMatcherAdaptor(mockArtifact("org.springframework", "spring-aop", "5.3.28"));
        assertEquals(0, res.size());
    }

    /**
     * test for adaptor: koupleless-adapter-rocketmq-4.4
     * pattern:
     *     matcher:
     *       groupId: org.apache.rocketmq
     *       artifactId: rocketmq-client
     *       versionRange: "[4.4.0,4.4.0]"
     *     adapter:
     *       artifactId: koupleless-adapter-rocketmq-4.4
     */
    @Test
    public void testMatcher5() throws InvalidVersionSpecificationException {
        List<Dependency> res = getMatcherAdaptor(mockArtifact("org.apache.rocketmq", "rocketmq-client", "4.4.0"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-rocketmq-4.4");

        res = getMatcherAdaptor(mockArtifact("org.apache.rocketmq", "rocketmq-client", "4.4.1.2"));
        assertEquals(0, res.size());
    }

    /**
     * test for adaptor: koupleless-adapter-apollo-1.6
     *     matcher:
     *       groupId: com.ctrip.framework.apollo
     *       artifactId: apollo-client
     *       versionRange: "(,)"
     *     adapter:
     *       artifactId: koupleless-adapter-apollo-1.6
     */
    @Test
    public void testMatcher6() throws InvalidVersionSpecificationException {
        List<Dependency> res = getMatcherAdaptor(mockArtifact("com.ctrip.framework.apollo", "apollo-client", "1.0.0"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-apollo-1.6");

        res = getMatcherAdaptor(mockArtifact("com.ctrip.framework.apollo", "apollo-client", "2.3.0"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-apollo-1.6");
    }


    /**
     * test for adaptor: koupleless-adapter-dubbo-2.6
     *     matcher:
     *       groupId: com.alibaba
     *       artifactId: dubbo-dependencies-bom
     *       versionRange: "[2.6.1,2.7.0)"
     *     adapter:
     *       artifactId: koupleless-adapter-dubbo-2.6
     */
    @Test
    public void testMatcher7() throws InvalidVersionSpecificationException {
        List<Dependency> res = getMatcherAdaptor(mockArtifact("com.alibaba", "dubbo-dependencies-bom", "2.6.1"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-dubbo-2.6");

        res = getMatcherAdaptor(mockArtifact("com.alibaba", "dubbo-dependencies-bom", "2.6.12"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-dubbo-2.6");

        res = getMatcherAdaptor(mockArtifact("com.alibaba", "dubbo-dependencies-bom", "2.7.0"));
        assertEquals(0, res.size());
    }


    /**
     * test for adaptor: koupleless-adapter-dubbo-2.6
     *     matcher:
     *       groupId: com.alibaba
     *       artifactId: dubbo
     *       versionRange: "[2.6.0,2.7.0)"
     *     adapter:
     *       artifactId: koupleless-adapter-dubbo-2.6
     */
    @Test
    public void testMatcher8() throws InvalidVersionSpecificationException {
        List<Dependency> res = getMatcherAdaptor(mockArtifact("com.alibaba", "dubbo", "2.6.0"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-dubbo-2.6");

        res = getMatcherAdaptor(mockArtifact("com.alibaba", "dubbo-dependencies-bom", "2.6.12"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-dubbo-2.6");

        res = getMatcherAdaptor(mockArtifact("com.alibaba", "dubbo-dependencies-bom", "2.8.4"));
        assertEquals(0, res.size());
    }

    /**
     * test for adaptor: koupleless-adapter-dubbo-2.7
     *     matcher:
     *       groupId: org.apache.dubbo
     *       artifactId: dubbo-spring-boot-starter
     *       versionRange: "[2.7.0,2.8.0)"
     *     adapter:
     *       artifactId: koupleless-adapter-dubbo-2.7
     */
    @Test
    public void testMatcher9() throws InvalidVersionSpecificationException {
        List<Dependency> res = getMatcherAdaptor(mockArtifact("org.apache.dubbo", "dubbo-spring-boot-starter", "2.7.0"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-dubbo-2.7");

        res = getMatcherAdaptor(mockArtifact("org.apache.dubbo", "dubbo-spring-boot-starter", "2.7.23"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-dubbo-2.7");

        res = getMatcherAdaptor(mockArtifact("org.apache.dubbo", "dubbo-spring-boot-starter", "3.0.0"));
        assertEquals(0, res.size());
    }


    /**
     * test for adaptor: koupleless-adapter-dubbo-2.7
     *     matcher:
     *       groupId: org.apache.dubbo
     *       artifactId: dubbo
     *       versionRange: "[2.7.0,2.8.0)"
     *     adapter:
     *       artifactId: koupleless-adapter-dubbo-2.7
     */
    @Test
    public void testMatcher10() throws InvalidVersionSpecificationException {
        List<Dependency> res = getMatcherAdaptor(mockArtifact("org.apache.dubbo", "dubbo", "2.7.0"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-dubbo-2.7");

        res = getMatcherAdaptor(mockArtifact("org.apache.dubbo", "dubbo", "2.7.23"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-dubbo-2.7");

        res = getMatcherAdaptor(mockArtifact("org.apache.dubbo", "dubbo", "3.0.0"));
        assertEquals(0, res.size());
    }

    /**
     * test for adaptor: koupleless-adapter-dubbo-3.2
     *     matcher:
     *       groupId: org.apache.dubbo
     *       artifactId: dubbo-common
     *       versionRange: "[3.2.0,3.3.0)"
     *     adapter:
     *       artifactId: koupleless-adapter-dubbo-3.2
     */
    @Test
    public void testMatcher11() throws InvalidVersionSpecificationException {
        List<Dependency> res = getMatcherAdaptor(mockArtifact("org.apache.dubbo", "dubbo-common", "3.2.0"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-dubbo-3.2");

        res = getMatcherAdaptor(mockArtifact("org.apache.dubbo", "dubbo-common", "3.2.1"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-dubbo-3.2");

        res = getMatcherAdaptor(mockArtifact("org.apache.dubbo", "dubbo-common", "3.3.0"));
        assertEquals(0, res.size());
    }

    /**
     * test for adaptor: koupleless-adapter-dubbo-3.1
     *     matcher:
     *       groupId: org.apache.dubbo
     *       artifactId: dubbo-common
     *       versionRange: "[3.1.0,3.2.0)"
     *     adapter:
     *       artifactId: koupleless-adapter-dubbo-3.1
     */
    @Test
    public void testMatcher12() throws InvalidVersionSpecificationException {
        List<Dependency> res = getMatcherAdaptor(mockArtifact("org.apache.dubbo", "dubbo-common", "3.1.0"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-dubbo-3.1");

        res = getMatcherAdaptor(mockArtifact("org.apache.dubbo", "dubbo-common", "3.1.1"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-dubbo-3.1");

        res = getMatcherAdaptor(mockArtifact("org.apache.dubbo", "dubbo-common", "3.2.0"));
        assertEquals(0, res.size());
    }

    /**
     * test for adaptor: koupleless-adapter-log4j2-spring-starter-2.1
     *     matcher:
     *       groupId: org.springframework.boot
     *       artifactId: spring-boot-starter-log4j2
     *       versionRange: "[2.1.0,2.4.0)"
     *     adapter:
     *       artifactId: koupleless-adapter-log4j2-spring-starter-2.1
     */
    @Test
    public void testMatcher15() throws InvalidVersionSpecificationException {
        List<Dependency> res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot-starter-log4j2", "2.1.0"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-log4j2-spring-starter-2.1");

        res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot-starter-log4j2", "2.3.0"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-log4j2-spring-starter-2.1");


        res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot-starter-log4j2", "2.4.0"));
        assertEquals(0, res.size());
    }

    /**
     * test for adaptor: koupleless-adapter-log4j2-spring-starter-2.4
     *     matcher:
     *       groupId: org.springframework.boot
     *       artifactId: spring-boot-starter-log4j2
     *       versionRange: "[2.4.0,2.7.0)"
     *     adapter:
     *       artifactId: koupleless-adapter-log4j2-spring-starter-2.4
     */
    @Test
    public void testMatcher16() throws InvalidVersionSpecificationException {
        List<Dependency> res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot-starter-log4j2", "2.4.0"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-log4j2-spring-starter-2.4");

        res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot-starter-log4j2", "2.6.0"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-log4j2-spring-starter-2.4");

        res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot-starter-log4j2", "2.7.0"));
        assertEquals(0, res.size());
    }

    /**
     * test for adaptor: koupleless-adapter-log4j2-spring-starter-2.7
     *     matcher:
     *       groupId: org.springframework.boot
     *       artifactId: spring-boot-starter-log4j2
     *       versionRange: "[2.7.0,3.0.0)"
     *     adapter:
     *       artifactId:koupleless-adapter-log4j2-spring-starter-2.7
     */
    @Test
    public void testMatcher17() throws InvalidVersionSpecificationException {
        List<Dependency> res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot-starter-log4j2", "2.7.0"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-log4j2-spring-starter-2.7");

        res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot-starter-log4j2", "3.0.0"));
        assertEquals(0, res.size());
    }


    /**
     * test for adaptor: koupleless-adapter-log4j2-spring-starter-3.0
     *     matcher:
     *       groupId: org.springframework.boot
     *       artifactId: spring-boot-starter-log4j2
     *       versionRange: "[3.0.0,3.2.0)"
     *     adapter:
     *       artifactId: koupleless-adapter-log4j2-spring-starter-3.0
     */
    @Test
    public void testMatcher13() throws InvalidVersionSpecificationException {
        List<Dependency> res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot-starter-log4j2", "3.0.0"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-log4j2-spring-starter-3.0");

        res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot-starter-log4j2", "3.1.0"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-log4j2-spring-starter-3.0");

        res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot-starter-log4j2", "3.2.0"));
        assertEquals(0, res.size());
    }

    /**
     * test for adaptor: koupleless-adapter-log4j2-spring-starter-3.2
     *     matcher:
     *       groupId: org.springframework.boot
     *       artifactId: spring-boot-starter-log4j2
     *       versionRange: "[3.2.0,)"
     *     adapter:
     *       artifactId: koupleless-adapter-log4j2-spring-starter-3.2
     */
    @Test
    public void testMatcher14() throws InvalidVersionSpecificationException {
        List<Dependency> res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot-starter-log4j2", "3.2.0"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-log4j2-spring-starter-3.2");

        res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot-starter-log4j2", "3.3.0"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-log4j2-spring-starter-3.2");
    }

    /**
     * test for adaptor: koupleless-adapter-logback-spring-starter-3.0
     *     matcher:
     *       groupId: org.springframework.boot
     *       artifactId: spring-boot-starter-logging
     *       versionRange: "[3.0.0,3.2.0)"
     *     adapter:
     *       artifactId: koupleless-adapter-logback-spring-starter-3.0
     */
    @Test
    public void testMatcher18() throws InvalidVersionSpecificationException {
        List<Dependency> res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot-starter-logging", "3.0.0"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-logback-spring-starter-3.0");

        res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot-starter-logging", "3.1.0"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-logback-spring-starter-3.0");

        res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot-starter-logging", "3.2.0"));
        assertEquals(0, res.size());
    }

    /**
     * test for adaptor: koupleless-adapter-logback-spring-starter-3.2
     *     matcher:
     *       groupId: org.springframework.boot
     *       artifactId: spring-boot-starter-logging
     *       versionRange: "[3.2.0,)"
     *     adapter:
     *       artifactId: koupleless-adapter-logback-spring-starter-3.2
     */
    @Test
    public void testMatcher19() throws InvalidVersionSpecificationException {
        List<Dependency> res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot-starter-logging", "3.2.0"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-logback-spring-starter-3.2");

        res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot-starter-logging", "3.3.0"));
        assertEquals(1, res.size());
        assertEquals(res.get(0).getArtifactId(), "koupleless-adapter-logback-spring-starter-3.2");
    }

    private List<Dependency> getMatcherAdaptor(Artifact artifact) throws InvalidVersionSpecificationException {
        List<Dependency> adapterDependencies = new ArrayList<>();
        for (MavenDependencyAdapterMapping adapterMapping : adapterMappings) {
            MavenDependencyMatcher matcher = adapterMapping.getMatcher();
            if (mojo.matches(matcher, artifact)) {
                adapterDependencies.add(adapterMapping.getAdapter());
            }
        }
        return adapterDependencies;
    }

    private Artifact mockArtifact(String groupId, String artifactId, String version) {
        Artifact artifact = Mockito.mock(Artifact.class);
        Mockito.when(artifact.getGroupId()).thenReturn(groupId);
        Mockito.when(artifact.getArtifactId()).thenReturn(artifactId);
        Mockito.when(artifact.getVersion()).thenReturn(version);
        return artifact;
    }
}
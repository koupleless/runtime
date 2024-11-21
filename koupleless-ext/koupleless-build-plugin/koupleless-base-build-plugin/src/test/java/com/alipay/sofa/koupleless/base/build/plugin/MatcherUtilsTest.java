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

    @Test
    public void testMatcher() throws InvalidVersionSpecificationException {

        List<Dependency> res = getMatcherAdaptor(
            mockArtifact("org.springframework.boot", "spring-boot", "2.7.14"));
        assertEquals(1, res.size());

        res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot", "2.5.1"));
        assertEquals(1, res.size());

        res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot", "2.6.5"));
        assertEquals(1, res.size());

        res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot", "2.5.0"));
        assertEquals(0, res.size());

        res = getMatcherAdaptor(mockArtifact("org.springframework.boot", "spring-boot", "2.7.15"));
        assertEquals(0, res.size());

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
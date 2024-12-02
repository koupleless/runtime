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
package com.alipay.sofa.koupleless.base.build.plugin.model;

import edu.emory.mathcs.backport.java.util.Collections;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.eclipse.aether.version.InvalidVersionSpecificationException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: KouplelessAdapterConfigTest.java, v 0.1 2024年12月01日 19:20 立蓬 Exp $
 */
public class KouplelessAdapterConfigTest {
    @Test
    public void testMatches() throws InvalidVersionSpecificationException {
        KouplelessAdapterConfig config = new KouplelessAdapterConfig();

        // init dependency
        Dependency dependency = new Dependency();
        dependency.setArtifactId("YYY");
        dependency.setGroupId("XXX");
        dependency.setVersion("1.0.0");

        // init artifacts
        List<Artifact> artifacts = new ArrayList<>();
        Artifact regexpArtifact = mockArtifact("groupId", "regexpArtifactId", "1.0.0", "classifier",
            "type");
        Artifact versionRangeArtifact = mockArtifact("groupId", "versionRangeArtifactId", "1.0.0",
            null, null);
        artifacts.add(regexpArtifact);
        artifacts.add(versionRangeArtifact);

        // match null
        assertEquals(0, config.matches(artifacts).size());

        // match with regexp
        MavenDependencyAdapterMapping regexpMapping = MavenDependencyAdapterMapping.builder()
            .matcher(MavenDependencyMatcher.builder().regexp(".*regexpArtifactId:1.*").build())
            .adapter(dependency).build();
        config.setAdapterMappings(Collections.singletonList(regexpMapping));
        assertEquals(1, config.matches(artifacts).size());

        // match with versionRange
        MavenDependencyAdapterMapping versionRangeMapping = MavenDependencyAdapterMapping.builder()
            .matcher(MavenDependencyMatcher.builder().groupId("groupId")
                .artifactId("versionRangeArtifactId").versionRange("[1.0.0,]").build())
            .adapter(dependency).build();
        config.setAdapterMappings(Collections.singletonList(versionRangeMapping));
        assertEquals(1, config.matches(artifacts).size());
    }

    private Artifact mockArtifact(String groupId, String artifactId, String version,
                                  String classifier, String type) {
        Artifact artifact = mock(Artifact.class);
        doReturn(groupId).when(artifact).getGroupId();
        doReturn(artifactId).when(artifact).getArtifactId();
        doReturn(version).when(artifact).getBaseVersion();
        doReturn(version).when(artifact).getVersion();
        doReturn(classifier).when(artifact).getClassifier();
        doReturn(type).when(artifact).getType();
        return artifact;
    }
}
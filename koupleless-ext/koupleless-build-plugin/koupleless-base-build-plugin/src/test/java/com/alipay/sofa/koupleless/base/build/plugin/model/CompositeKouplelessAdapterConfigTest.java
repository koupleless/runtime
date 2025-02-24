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

import com.alipay.sofa.koupleless.base.build.plugin.KouplelessBaseBuildPrePackageMojo;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.version.Version;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alipay.sofa.koupleless.utils.MockUtils.getResourceAsFile;
import static com.alipay.sofa.koupleless.utils.ReflectionUtils.getField;
import static com.alipay.sofa.koupleless.utils.ReflectionUtils.setField;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: CompositeKouplelessAdapterConfigTest.java, v 0.1 2024年12月01日 19:20 立蓬 Exp $
 */
public class CompositeKouplelessAdapterConfigTest {
    @Test
    public void testInitCustomConfig() throws URISyntaxException {
        KouplelessBaseBuildPrePackageMojo mojo = new KouplelessBaseBuildPrePackageMojo();
        mojo.baseDir = getResourceAsFile("mockBaseDir");

        CompositeKouplelessAdapterConfig config = new CompositeKouplelessAdapterConfig();
        config.initCustomConfig(mojo);

        KouplelessAdapterConfig customConfig = getField("customConfig", config);
        assertNotNull(customConfig);
    }

    @Test
    public void testInitRemoteConfig() throws Exception {
        KouplelessBaseBuildPrePackageMojo mojo = spy(new KouplelessBaseBuildPrePackageMojo());

        {
            // mock for parseRemoteConfigVersion
            RepositorySystem repositorySystem = mock(RepositorySystem.class);

            VersionRangeResult rangeResult = mock(VersionRangeResult.class);
            doReturn(new Version() {
                @Override
                public int compareTo(Version o) {
                    return 0;
                }

                @Override
                public String toString() {
                    return "1.0.0";
                }
            }).when(rangeResult).getHighestVersion();
            doReturn(rangeResult).when(repositorySystem).resolveVersionRange(any(), any());

            mojo.repositorySystem = repositorySystem;

            List<RemoteRepository> remoteRepositories = Collections.emptyList();
            mojo.project = mock(MavenProject.class);
            doReturn(remoteRepositories).when(mojo.project).getRemoteProjectRepositories();

            RepositorySystemSession repositorySystemSession = mock(RepositorySystemSession.class);
            mojo.session = mock(MavenSession.class);
            doReturn(repositorySystemSession).when(mojo.session).getRepositorySession();
        }

        {
            // mock for downloadAdapterConfigsJar
            Artifact artifact = mock(Artifact.class);
            doReturn(getResourceAsFile("koupleless-adapter-configs-1.2.3.jar")).when(artifact)
                .getFile();
            doReturn(artifact).when(mojo).downloadDependency(any());
        }

        CompositeKouplelessAdapterConfig config = new CompositeKouplelessAdapterConfig();
        config.initRemoteConfig(mojo);

        List<KouplelessAdapterConfig> remoteConfigs = getField("remoteConfigs", config);
        assertEquals(18, remoteConfigs.size());
    }

    @Test
    public void testGetCommonDependencies() {
        CompositeKouplelessAdapterConfig config = new CompositeKouplelessAdapterConfig();

        KouplelessAdapterConfig customConfig = mock(KouplelessAdapterConfig.class);
        setField("customConfig", config, customConfig);

        List<KouplelessAdapterConfig> remoteConfigs = getField("remoteConfigs", config);
        KouplelessAdapterConfig remoteConfig = mock(KouplelessAdapterConfig.class);
        remoteConfigs.add(remoteConfig);
    }

    @Test
    public void testMatches() {
        CompositeKouplelessAdapterConfig config = new CompositeKouplelessAdapterConfig();

        KouplelessAdapterConfig customConfig = mock(KouplelessAdapterConfig.class);
        Map<MavenDependencyAdapterMapping, org.apache.maven.artifact.Artifact> custom = new HashMap<>();
        custom.put(mock(MavenDependencyAdapterMapping.class),
            mock(org.apache.maven.artifact.Artifact.class));
        doReturn(custom).when(customConfig).matches(any());
        setField("customConfig", config, customConfig);

        List<KouplelessAdapterConfig> remoteConfigs = getField("remoteConfigs", config);
        KouplelessAdapterConfig remoteConfig = mock(KouplelessAdapterConfig.class);
        remoteConfigs.add(remoteConfig);
        Map<MavenDependencyAdapterMapping, org.apache.maven.artifact.Artifact> remote = new HashMap<>();
        remote.put(mock(MavenDependencyAdapterMapping.class),
            mock(org.apache.maven.artifact.Artifact.class));
        doReturn(remote).when(remoteConfig).matches(any());

        Map<MavenDependencyAdapterMapping, org.apache.maven.artifact.Artifact> matches = config
            .matches(asList(mock(org.apache.maven.artifact.Artifact.class)));
        assertEquals(2, matches.size());
    }

    private Dependency mockDependency(String groupId, String artifactId, String version) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        return dependency;
    }
}

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

import com.alipay.sofa.koupleless.base.build.plugin.model.CompositeKouplelessAdapterConfig;
import com.alipay.sofa.koupleless.base.build.plugin.model.KouplelessAdapterConfig;
import com.alipay.sofa.koupleless.base.build.plugin.model.MavenDependencyAdapterMapping;
import com.alipay.sofa.koupleless.base.build.plugin.model.MavenDependencyMatcher;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author CodeNoobKing
 * @since 2024/2/19
 */
public class KouplelessBaseBuildPrePackageMojoTest {
    // create a tmp directory using os tmp
    private File outputDirectory = null;

    @Test
    public void testLazyInitKouplelessAdapterConfig() throws Exception {
        KouplelessBaseBuildPrePackageMojo mojo = new KouplelessBaseBuildPrePackageMojo();
        mojo.kouplelessAdapterConfig = mock(CompositeKouplelessAdapterConfig.class);

        mojo.initKouplelessAdapterConfig();
        assertEquals("com.alipay.sofa.koupleless", mojo.defaultGroupId);
        assertTrue(StringUtils.isNoneEmpty(mojo.defaultVersion));
    }

    @Test
    public void testAddDependencyDynamically() throws Exception {
        KouplelessBaseBuildPrePackageMojo mojo = new KouplelessBaseBuildPrePackageMojo();
        MavenProject mockProject = mock(MavenProject.class);
        // init maven project
        Set<org.apache.maven.artifact.Artifact> artifacts = new HashSet<>();
        org.apache.maven.artifact.Artifact artifact = mock(
            org.apache.maven.artifact.Artifact.class);
        doReturn("A").when(artifact).getGroupId();
        //            doReturn("B").when(artifact).getArtifactId();
        doReturn("C").when(artifact).getBaseVersion();
        artifacts.add(artifact);
        doReturn(artifacts).when(mockProject).getArtifacts();
        // set resolvedArtifacts in project
        Field field = MavenProject.class.getDeclaredField("resolvedArtifacts");
        field.setAccessible(true);
        field.set(mockProject, artifacts);

        Field projectField = KouplelessBaseBuildPrePackageMojo.class.getSuperclass().getSuperclass()
            .getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(mojo, mockProject);

        {
            // init the adapter config
            Dependency mockDependency = new Dependency();
            mockDependency.setGroupId("XXX");
            mockDependency.setArtifactId("YYY");
            mockDependency.setVersion("ZZZ");

            Map<MavenDependencyAdapterMapping, org.apache.maven.artifact.Artifact> matchedResult = new HashMap<>();
            mojo.kouplelessAdapterConfig = mock(CompositeKouplelessAdapterConfig.class);

            List<MavenDependencyAdapterMapping> mappings = new ArrayList<>();
            mappings.add(MavenDependencyAdapterMapping.builder().adapter(mockDependency)
                .matcher(MavenDependencyMatcher.builder().regexp(".*A:B:C.*").build()).build());
            matchedResult.put(mappings.get(0), artifact);
            doReturn(matchedResult).when(mojo.kouplelessAdapterConfig).matches(any());
        }

        {
            // mock the repository system
            ArtifactResult mockArtifactResult = new ArtifactResult(new ArtifactRequest());
            Artifact mockArtifact = mock(Artifact.class);
            mockArtifactResult.setArtifact(mockArtifact);

            URL demoJarUrl = getClass().getClassLoader().getResource("demo.jar");
            doReturn(new File(demoJarUrl.toURI())).when(mockArtifact).getFile();

            RepositorySystem repositorySystem = mock(RepositorySystem.class);
            mojo.repositorySystem = repositorySystem;
            doReturn(mockArtifactResult).when(repositorySystem).resolveArtifact(any(), any());
        }

        {
            // mock the session
            MavenSession session = mock(MavenSession.class);
            Field sessionField = KouplelessBaseBuildPrePackageMojo.class.getSuperclass()
                .getSuperclass().getField("session");
            sessionField.setAccessible(true);
            sessionField.set(mojo, session);
        }

        {
            // init output directory
            Field outputDirectoryField = KouplelessBaseBuildPrePackageMojo.class.getSuperclass()
                .getDeclaredField("outputDirectory");
            outputDirectoryField.setAccessible(true);
            outputDirectoryField.set(mojo, Files.createTempDirectory("mojotest").toFile());

            Field compileSourceRoots = KouplelessBaseBuildPrePackageMojo.class.getSuperclass()
                .getDeclaredField("compileSourceRoots");
            compileSourceRoots.setAccessible(true);
            compileSourceRoots.set(mojo, new ArrayList<>());
        }

        mojo.execute();

        {
            Assert.assertTrue(Paths.get(mojo.getOutputDirectory().getAbsolutePath(), "classes",
                "com", "example", "demo").toFile().exists());
        }
    }

}

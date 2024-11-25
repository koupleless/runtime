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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author CodeNoobKing
 * @since 2024/2/19
 */
@RunWith(MockitoJUnitRunner.class)
public class KouplelessBaseBuildPrePackageMojoTest {
    @InjectMocks
    private KouplelessBaseBuildPrePackageMojo mojo;

    // create a tmp directory using os tmp
    private File                              outputDirectory = null;

    @Mock
    MavenProject                              project;

    @Mock
    MavenSession                              session;

    @Mock
    RepositorySystem                          repositorySystem;

    @Before
    public void setUp() {
        mojo.MAPPING_FILE = "adapter-mapping-ext.yml";
    }

    @Test
    public void testLazyInitKouplelessAdapterConfig() throws Exception {
        Dependency mockDependency = new Dependency();
        mockDependency.setGroupId("XXX");
        mockDependency.setArtifactId("YYY");
        mockDependency.setVersion("ZZZ");

        List<Dependency> commonDependencies = new ArrayList<>();
        commonDependencies.add(mockDependency);

        List<MavenDependencyAdapterMapping> mappings = new ArrayList<>();
        mappings.add(MavenDependencyAdapterMapping.builder().adapter(mockDependency)
            .matcher(MavenDependencyMatcher.builder().regexp(".*").build()).build());

        mojo.initKouplelessAdapterConfig();
        KouplelessAdapterConfig expected = KouplelessAdapterConfig.builder()
            .commonDependencies(commonDependencies).adapterMappings(mappings).build();

        Assert.assertEquals(expected.getCommonDependencies().toString(), mojo.kouplelessAdapterConfig.getCommonDependencies().toString());

        Assert.assertEquals(expected.getAdapterMappings().stream().map(MavenDependencyAdapterMapping::getMatcher).findFirst().get().getRegexp(),
            mojo.kouplelessAdapterConfig.getAdapterMappings().stream().map(MavenDependencyAdapterMapping::getMatcher).findFirst().get().getRegexp());

        Assert.assertEquals(expected.getAdapterMappings().stream().map(MavenDependencyAdapterMapping::getAdapter).findFirst().get().toString(),
                mojo.kouplelessAdapterConfig.getAdapterMappings().stream().map(MavenDependencyAdapterMapping::getAdapter).findFirst().get().toString());
    }

    @Test
    public void testAddDependencyDynamically() throws Exception {
        {
            // init the adapter config
            Dependency mockDependency = new Dependency();
            mockDependency.setGroupId("XXX");
            mockDependency.setArtifactId("YYY");
            mockDependency.setVersion("ZZZ");

            List<Dependency> commonDependencies = new ArrayList<>();
            commonDependencies.add(mockDependency);

            List<MavenDependencyAdapterMapping> mappings = new ArrayList<>();
            mappings.add(MavenDependencyAdapterMapping.builder().adapter(mockDependency)
                .matcher(MavenDependencyMatcher.builder().regexp(".*A:B:C.*").build()).build());

            mojo.kouplelessAdapterConfig = KouplelessAdapterConfig.builder()
                .commonDependencies(commonDependencies).adapterMappings(mappings).build();
        }

        {
            // init maven project
            Set<org.apache.maven.artifact.Artifact> artifacts = new HashSet<>();
            org.apache.maven.artifact.Artifact artifact = mock(
                org.apache.maven.artifact.Artifact.class);
            doReturn("A").when(artifact).getGroupId();
            //            doReturn("B").when(artifact).getArtifactId();
            doReturn("C").when(artifact).getBaseVersion();
            artifacts.add(artifact);
            project.setArtifacts(artifacts);
            // set resolvedArtifacts in project
            Field field = MavenProject.class.getDeclaredField("resolvedArtifacts");
            field.setAccessible(true);
            field.set(project, artifacts);
        }

        {
            // mock the repository system
            ArtifactResult mockArtifactResult = new ArtifactResult(new ArtifactRequest());
            Artifact mockArtifact = mock(Artifact.class);
            mockArtifactResult.setArtifact(mockArtifact);

            URL demoJarUrl = getClass().getClassLoader().getResource("demo.jar");
            doReturn(new File(demoJarUrl.toURI())).when(mockArtifact).getFile();
            doReturn(mockArtifactResult).when(repositorySystem).resolveArtifact(any(), any());
        }

        {
            // init output directory
            mojo.outputDirectory = Files.createTempDirectory("mojotest").toFile();
        }

        mojo.execute();

        {
            Assert.assertTrue(Paths
                .get(mojo.outputDirectory.getAbsolutePath(), "classes", "com", "example", "demo")
                .toFile().exists());
        }
    }

}

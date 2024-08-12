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

import com.alipay.sofa.koupleless.base.build.plugin.utils.MavenUtils;
import com.alipay.sofa.koupleless.utils.CommonUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.net.URISyntaxException;

import static com.alipay.sofa.koupleless.utils.ReflectionUtils.setField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: KouplelessBasePackageDependencyMojoTest.java, v 0.1 2024年07月22日 10:21 立蓬 Exp $
 */
public class KouplelessBasePackageDependencyMojoTest {

    private MavenProject bootstrapProject = getMockBootstrapProject();

    public KouplelessBasePackageDependencyMojoTest() throws URISyntaxException {
    }

    @Test
    public void testExecute() throws MojoExecutionException, MojoFailureException,
                              URISyntaxException, MavenInvocationException {
        KouplelessBasePackageDependencyMojo mojo = spy(createMojo());
        doNothing().when(mojo).installBaseDependencies();

        mojo.execute();

        assertTrue(CommonUtils
            .resourceExists("mockBaseDir/base-bootstrap/base-all-dependencies-starter/pom.xml"));

        Model pom = MavenUtils.buildPomModel(CommonUtils
            .getResourceFile("mockBaseDir/base-bootstrap/base-all-dependencies-starter/pom.xml"));

        assertEquals(2, pom.getDependencyManagement().getDependencies().size());
        assertEquals(1, pom.getDependencyManagement().getDependencies().stream()
            .filter(d -> d.getArtifactId().equals("a1")).count());
        Dependency d2 = pom.getDependencyManagement().getDependencies().stream()
            .filter(d -> d.getArtifactId().equals("a2")).findFirst().get();
        assertFalse(d2.getExclusions().isEmpty());
    }

    @Test
    public void testInstallBaseDependencies() throws MojoExecutionException,
                                              MavenInvocationException {
        KouplelessBasePackageDependencyMojo mojo = new KouplelessBasePackageDependencyMojo();
        try (MockedStatic<MavenUtils> mavenUtilMockedStatic = Mockito
            .mockStatic(MavenUtils.class)) {
            InvocationResult result = mock(InvocationResult.class);

            // case 1: install base dependencies success
            when(result.getExitCode()).thenReturn(0);
            mavenUtilMockedStatic
                .when(() -> MavenUtils.invoke(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(result);
            mojo.installBaseDependencies();

            // case 2: install base dependencies failed
            when(result.getExitCode()).thenReturn(1);
            try {
                mojo.installBaseDependencies();
            } catch (Exception e) {
                assertEquals("execute mvn install failed for base dependencies", e.getMessage());
            }
        }
    }

    private MavenProject getMockBootstrapProject() throws URISyntaxException {
        MavenProject project = new MavenProject();
        project.setArtifactId("base-bootstrap");
        project.setGroupId("com.mock");
        project.setVersion("0.0.1-SNAPSHOT");
        project.setPackaging("jar");
        project.setFile(CommonUtils.getResourceFile("mockBaseDir/base-bootstrap/pom.xml"));
        project.setParent(getRootProject());
        project.setProjectBuildingRequest(new DefaultProjectBuildingRequest());

        Artifact a1 = mock(Artifact.class);
        when(a1.getArtifactId()).thenReturn("a1");
        when(a1.getGroupId()).thenReturn("com.mock.outside");
        when(a1.getVersion()).thenReturn("1.0.0");
        when(a1.getBaseVersion()).thenReturn("1.0.0");

        Artifact a2 = mock(Artifact.class);
        when(a2.getArtifactId()).thenReturn("a2");
        when(a2.getGroupId()).thenReturn("com.mock.outside");
        when(a2.getVersion()).thenReturn("1.0.0");
        when(a2.getBaseVersion()).thenReturn("1.0.0");
        project.setArtifacts(Sets.newHashSet(a1, a2));

        MavenProject spyProject = spy(project);
        DependencyManagement dm = new DependencyManagement();
        Dependency d2 = MavenUtils.createDependency(a2);

        Exclusion exclusion = new Exclusion();
        exclusion.setArtifactId("e1");
        exclusion.setGroupId("com.mock.exclustion");
        d2.setExclusions(Lists.newArrayList(exclusion));
        dm.setDependencies(Lists.newArrayList(d2));
        doReturn(dm).when(spyProject).getDependencyManagement();
        return spyProject;
    }

    private MavenProject getRootProject() throws URISyntaxException {
        MavenProject project = new MavenProject();
        project.setArtifactId("base");
        project.setGroupId("com.mock");
        project.setVersion("0.0.1-SNAPSHOT");
        project.setPackaging("pom");
        project.setFile(CommonUtils.getResourceFile("mockBaseDir/pom.xml"));
        project.setParent(null);

        Model model = MavenUtils.buildPomModel(project.getFile());
        project.setOriginalModel(model);
        project.setModel(model);
        return project;
    }

    private KouplelessBasePackageDependencyMojo createMojo() {
        KouplelessBasePackageDependencyMojo mojo = new KouplelessBasePackageDependencyMojo();
        setField("mavenProject", mojo, bootstrapProject);
        setField("dependencyArtifactId", mojo, "base-all-dependencies-starter");
        setField("dependencyVersion", mojo, "1.0.0");
        setField("dependencyGroupId", mojo, "com.mock");
        setField("baseDir", mojo, bootstrapProject.getBasedir());
        setField("cleanAfterPackageDependencies", mojo, "false");

        MavenSession mavenSession = mock(MavenSession.class);
        doReturn(new Settings()).when(mavenSession).getSettings();
        doReturn(new DefaultMavenExecutionRequest()).when(mavenSession).getRequest();
        doReturn(bootstrapProject).when(mavenSession).getCurrentProject();
        setField("mavenSession", mojo, mavenSession);
        return mojo;
    }
}
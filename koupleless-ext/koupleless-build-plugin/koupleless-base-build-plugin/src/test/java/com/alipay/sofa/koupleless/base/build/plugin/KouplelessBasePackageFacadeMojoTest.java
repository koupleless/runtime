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
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.alipay.sofa.koupleless.base.build.plugin.KouplelessBasePackageFacadeMojo.JVMFileTypeEnum.JAVA;
import static com.alipay.sofa.koupleless.base.build.plugin.KouplelessBasePackageFacadeMojo.JVMFileTypeEnum.KOTLIN;
import static com.alipay.sofa.koupleless.utils.ReflectionUtils.setField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: KouplelessBasePackageFacadeMojoTest.java, v 0.1 2024年07月22日 10:21 立蓬 Exp $
 */
public class KouplelessBasePackageFacadeMojoTest {
    private MavenProject bootstrapProject = getMockBootstrapProject();

    public KouplelessBasePackageFacadeMojoTest() throws URISyntaxException {
    }

    @Test
    public void testExecute() throws Exception {
        KouplelessBasePackageFacadeMojo mojo = spy(createMojo());
        doNothing().when(mojo).installBaseFacades();
        doNothing().when(mojo).moveToOutputs();
        mojo.execute();

        // 1. verify the pom of base-all-dependencies-facade
        String pomPath = "mockBaseDir/base-bootstrap/base-all-dependencies-facade/pom.xml";
        assertTrue(CommonUtils.resourceExists(pomPath));

        Model pom = MavenUtils.buildPomModel(CommonUtils.getResourceFile(pomPath));
        Dependency d = pom.getDependencies().stream().findFirst().get();
        assertEquals("a1", d.getArtifactId());
        assertEquals("provided", d.getScope());

        // 2. verify the kotlin file copy
        assertTrue(CommonUtils.resourceExists(
            "mockBaseDir/base-bootstrap/base-all-dependencies-facade/src/main/kotlin/com/mock/base/facade/ModuleDescriptionInfo.kt"));

        setField("cleanAfterPackageFacade", mojo, "true");
        mojo.clearFacadeRootDir();
    }

    @Test
    public void testInstallBaseFacades() throws MojoExecutionException, MavenInvocationException {
        KouplelessBasePackageFacadeMojo mojo = new KouplelessBasePackageFacadeMojo();
        try (MockedStatic<MavenUtils> mavenUtilMockedStatic = Mockito
            .mockStatic(MavenUtils.class)) {
            InvocationResult result = mock(InvocationResult.class);

            // case 1: install base facades success
            when(result.getExitCode()).thenReturn(0);
            mavenUtilMockedStatic
                .when(() -> MavenUtils.invoke(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(result);
            mojo.installBaseFacades();

            // case 2: install base facades failed
            when(result.getExitCode()).thenReturn(1);
            try {
                mojo.installBaseFacades();
            } catch (Exception e) {
                assertEquals("execute mvn install failed for base facades", e.getMessage());
            }
        }
    }

    @Test
    public void testGetSupportedJVMFiles() throws URISyntaxException {
        List<File> files = KouplelessBasePackageFacadeMojo
            .getSupportedJVMFiles(CommonUtils.getResourceFile("mockBaseDir"));
        assertEquals(3, files.size());
    }

    @Test
    public void testParseFullClassName() throws URISyntaxException {
        assertEquals("com.mock.base.bootstrap.BootstrapModel",
            JAVA.parseFullClassName(CommonUtils.getResourceFile(
                "mockBaseDir/base-bootstrap/src/main/java/com/mock/base/bootstrap/BootstrapModel.java")));
        assertEquals("com.mock.base.facade.ModuleDescriptionInfo",
            KOTLIN.parseFullClassName(CommonUtils.getResourceFile(
                "mockBaseDir/base-facade/src/main/kotlin/com/mock/base/facade/ModuleDescriptionInfo.kt")));
    }

    @Test
    public void testMatches() throws URISyntaxException {
        assertTrue(JAVA.matches(CommonUtils.getResourceFile(
            "mockBaseDir/base-bootstrap/src/main/java/com/mock/base/bootstrap/BootstrapModel.java")));
        assertTrue(KOTLIN.matches(CommonUtils.getResourceFile(
            "mockBaseDir/base-facade/src/main/kotlin/com/mock/base/facade/ModuleDescriptionInfo.kt")));

        assertFalse(KOTLIN.matches(CommonUtils.getResourceFile(
            "mockBaseDir/base-bootstrap/src/main/java/com/mock/base/bootstrap/BootstrapModel.java")));
        assertFalse(JAVA.matches(CommonUtils
            .getResourceFile("mockBaseDir/base-bootstrap/src/main/resources/BootstrapModel.java")));
    }

    @Test
    public void testParseRelativePath() throws URISyntaxException {
        assertEquals(
            StringUtils.join(new String[] { "src", "main", "java", "com", "mock", "base",
                                            "bootstrap", "BootstrapModel.java" },
                File.separator),
            JAVA.parseRelativePath(CommonUtils.getResourceFile(
                "mockBaseDir/base-bootstrap/src/main/java/com/mock/base/bootstrap/BootstrapModel.java")));
        assertNull(KOTLIN.parseRelativePath(CommonUtils.getResourceFile(
            "mockBaseDir/base-bootstrap/src/main/java/com/mock/base/bootstrap/BootstrapModel.java")));
    }

    @Test
    public void testGetBaseModuleArtifactIds() throws Exception {
        KouplelessBasePackageFacadeMojo mojo = new KouplelessBasePackageFacadeMojo();
        Field field = KouplelessBasePackageFacadeMojo.class.getDeclaredField("mavenProject");
        field.setAccessible(true);
        field.set(mojo, bootstrapProject);

        Method method = KouplelessBasePackageFacadeMojo.class
            .getDeclaredMethod("getBaseModuleArtifactIds");
        method.setAccessible(true);
        Set<String> moduleArtifactIds = (Set<String>) method.invoke(mojo);

        assertTrue(moduleArtifactIds.contains("base-bootstrap"));
        assertTrue(moduleArtifactIds.contains("base-facade"));
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
        project.setArtifacts(Collections.singleton(a1));
        return project;
    }

    private MavenProject getRootProject() throws URISyntaxException {
        MavenProject project = new MavenProject();
        project.setArtifactId("base");
        project.setGroupId("com.mock");
        project.setVersion("0.0.1-SNAPSHOT");
        project.setPackaging("pom");
        project.setFile(CommonUtils.getResourceFile("mockBaseDir/pom.xml"));
        project.setParent(null);
        project.setModel(MavenUtils.buildPomModel(project.getFile()));
        return project;
    }

    private KouplelessBasePackageFacadeMojo createMojo() {
        KouplelessBasePackageFacadeMojo mojo = new KouplelessBasePackageFacadeMojo();
        setField("mavenProject", mojo, bootstrapProject);
        setField("facadeArtifactId", mojo, "base-all-dependencies-facade");
        setField("facadeVersion", mojo, "1.0.0");
        setField("facadeGroupId", mojo, "com.mock");
        setField("baseDir", mojo, bootstrapProject.getBasedir());
        setField("cleanAfterPackageFacade", mojo, "false");
        LinkedHashSet<String> jvmFiles = new LinkedHashSet<String>(
            Collections.singletonList("com.mock.base.facade.ModuleDescriptionInfo"));
        setField("jvmFiles", mojo, jvmFiles);

        MavenSession mavenSession = mock(MavenSession.class);
        doReturn(new Settings()).when(mavenSession).getSettings();
        doReturn(new DefaultMavenExecutionRequest()).when(mavenSession).getRequest();
        doReturn(bootstrapProject).when(mavenSession).getCurrentProject();
        setField("mavenSession", mojo, mavenSession);
        return mojo;
    }

}
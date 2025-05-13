/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2025 All Rights Reserved.
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
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.Test;

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
 * @version $Id: KouplelessBasePackagePluginDependencyMojoTest.java, v 0.1 2025年05月13日 20:42 立蓬 Exp $
 */
public class KouplelessBasePackagePluginDependencyMojoTest {
    private MavenProject bootstrapProject = getMockBootstrapProject();

    public KouplelessBasePackagePluginDependencyMojoTest() throws URISyntaxException {

    }

    @Test
    public void testExecute() throws MojoExecutionException, MojoFailureException,
            URISyntaxException, MavenInvocationException {
        KouplelessBasePackagePluginDependencyMojo mojo = spy(createMojo());
        doNothing().when(mojo).installPluginDependencies();

        mojo.execute();

        assertTrue(CommonUtils
                .resourceExists("mockBizPluginDir/biz-plugin-bom/pom.xml"));

        Model pom = MavenUtils.buildPomModel(CommonUtils
                .getResourceFile("mockBizPluginDir/biz-plugin-bom/pom.xml"));

        assertEquals(1, pom.getDependencyManagement().getDependencies().size());
        assertEquals(1, pom.getDependencyManagement().getDependencies().stream()
                .filter(d -> d.getArtifactId().equals("hutool-all")).count());
    }


    private MavenProject getMockBootstrapProject() throws URISyntaxException {
        MavenProject project = new MavenProject();
        project.setArtifactId("biz-plugin");
        project.setGroupId("com.mock");
        project.setVersion("0.0.1-SNAPSHOT");
        project.setPackaging("jar");
        project.setFile(CommonUtils.getResourceFile("mockBizPluginDir/pom.xml"));
        project.setParent(getRootProject());
        project.setProjectBuildingRequest(new DefaultProjectBuildingRequest());

        Artifact bizPluginArtifact = mock(Artifact.class);
        when(bizPluginArtifact.getArtifactId()).thenReturn("biz-plugin");
        when(bizPluginArtifact.getGroupId()).thenReturn("com.mock");
        when(bizPluginArtifact.getVersion()).thenReturn("0.0.1-SNAPSHOT");
        project.setArtifact(bizPluginArtifact);


        Model model = MavenUtils.buildPomModel(project.getFile());
        project.setOriginalModel(model);
        project.setModel(model);

        Artifact a1 = mock(Artifact.class);
        when(a1.getArtifactId()).thenReturn("hutool-all");
        when(a1.getGroupId()).thenReturn("cn.hutool");
        when(a1.getVersion()).thenReturn("5.8.25");
        when(a1.getBaseVersion()).thenReturn("5.8.25");
        when(a1.getType()).thenReturn("jar");
        when(a1.getScope()).thenReturn("compile");

        Artifact a2 = mock(Artifact.class);
        when(a2.getArtifactId()).thenReturn("tomcat-embed-core");
        when(a2.getGroupId()).thenReturn("org.apache.tomcat.embed");
        when(a2.getVersion()).thenReturn("9.0.85");
        when(a2.getBaseVersion()).thenReturn("9.0.85");
        when(a2.getType()).thenReturn("jar");
        when(a2.getScope()).thenReturn("compile");

        Artifact a3 = mock(Artifact.class);
        when(a3.getArtifactId()).thenReturn("biz-plugin-facade");
        when(a3.getGroupId()).thenReturn("com.mock");
        when(a3.getVersion()).thenReturn("0.0.1-SNAPSHOT");
        when(a3.getBaseVersion()).thenReturn("0.0.1-SNAPSHOT");
        when(a3.getType()).thenReturn("jar");
        when(a3.getScope()).thenReturn("compile");
        project.setArtifacts(Sets.newHashSet(a1, a2,a3));

        return project;
    }

    private MavenProject getRootProject() throws URISyntaxException {
        MavenProject project = new MavenProject();
        project.setArtifactId("base-dependencies-starter");
        project.setGroupId("com.mock");
        project.setVersion("0.0.1-SNAPSHOT");
        project.setPackaging("pom");
        project.setFile(CommonUtils.getResourceFile("mockBizPluginDir/baseDependenciesStarterPom.xml"));
        project.setParent(null);

        Model model = MavenUtils.buildPomModel(project.getFile());
        project.setOriginalModel(model);
        project.setModel(model);

        Artifact bizPluginArtifact = mock(Artifact.class);
        when(bizPluginArtifact.getArtifactId()).thenReturn("base-dependencies-starter");
        when(bizPluginArtifact.getGroupId()).thenReturn("com.mock");
        when(bizPluginArtifact.getVersion()).thenReturn("0.0.1-SNAPSHOT");
        project.setArtifact(bizPluginArtifact);
        return project;
    }

    private KouplelessBasePackagePluginDependencyMojo createMojo() {
        KouplelessBasePackagePluginDependencyMojo mojo = new KouplelessBasePackagePluginDependencyMojo();
        setField("mavenProject", mojo, bootstrapProject);
        setField("pluginDependencyArtifactId", mojo, "biz-plugin-bom");
        setField("pluginDependencyVersion", mojo, "1.0.0");
        setField("pluginDependencyGroupId", mojo, "com.mock");
        setField("cleanAfterPackageDependencies", mojo, "false");
        setField("baseDir", mojo, bootstrapProject.getBasedir());
        setField("baseDependencyParentIdentity", mojo, "com.mock:base-dependencies-starter");

        MavenSession mavenSession = mock(MavenSession.class);
        doReturn(new Settings()).when(mavenSession).getSettings();
        doReturn(new DefaultMavenExecutionRequest()).when(mavenSession).getRequest();
        doReturn(bootstrapProject).when(mavenSession).getCurrentProject();
        setField("mavenSession", mojo, mavenSession);
        return mojo;
    }
}
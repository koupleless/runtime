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

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.alipay.sofa.koupleless.base.build.plugin.adapter.AdapterCopyService;
import com.alipay.sofa.koupleless.base.build.plugin.common.JarFileUtils;
import com.alipay.sofa.koupleless.base.build.plugin.model.CompositeKouplelessAdapterConfig;
import com.alipay.sofa.koupleless.base.build.plugin.model.MavenDependencyAdapterMapping;
import com.alipay.sofa.koupleless.base.build.plugin.model.MavenDependencyMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.version.InvalidVersionSpecificationException;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.alipay.sofa.koupleless.base.build.plugin.utils.ParseUtils.parseVersion;

/**
 * Goal which touches a timestamp file.
 *
 * @author zzl_i
 * @version 1.0.0
 */
@Mojo(name = "add-patch", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class KouplelessBaseBuildPrePackageMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    File                                       outputDirectory;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    public MavenProject                        project;

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    public MavenSession                        session;

    @Parameter(defaultValue = "${project.basedir}", required = true)
    public File                                baseDir;

    @Component
    public RepositorySystem                    repositorySystem;

    protected CompositeKouplelessAdapterConfig kouplelessAdapterConfig;

    @Parameter(defaultValue = "", required = false)
    public String                              kouplelessAdapterConfigVersion;

    @Parameter(defaultValue = "", required = false)
    public String                              customAdaptorMapping = "";

    AdapterCopyService                         adapterCopyService   = new AdapterCopyService();

    String                                     defaultGroupId       = "";
    String                                     defaultVersion       = "";

    void initKouplelessAdapterConfig() throws Exception {
        if (kouplelessAdapterConfig == null) {
            kouplelessAdapterConfig = new CompositeKouplelessAdapterConfig(this);
        }

        Properties properties = new Properties();
        properties.load(getClass().getClassLoader().getResourceAsStream("project.properties"));
        defaultGroupId = properties.getProperty("project.groupId");
        defaultVersion = properties.getProperty("project.version");
    }

    // visible for testing
    List<Dependency> getDependenciesToAdd() throws Exception {
        List<Dependency> adapterDependencies = new ArrayList<>();
        if (kouplelessAdapterConfig == null) {
            getLog().info("kouplelessAdapterConfig is null, skip adding dependencies.");
            return adapterDependencies;
        }

        if (kouplelessAdapterConfig.getCommonDependencies() != null) {
            adapterDependencies.addAll(kouplelessAdapterConfig.getCommonDependencies());
        }

        // get resolvedArtifacts from project by reflection
        Field field = MavenProject.class.getDeclaredField("resolvedArtifacts");
        field.setAccessible(true);
        Set<org.apache.maven.artifact.Artifact> resolvedArtifacts = (Set<org.apache.maven.artifact.Artifact>) field
            .get(project);
        if (resolvedArtifacts == null) {
            return adapterDependencies;
        }

        // get dependencies by matching
        adapterDependencies.addAll(getDependenciesByMatching(resolvedArtifacts));

        return adapterDependencies;
    }

    List<Dependency> getDependenciesByMatching(Collection<org.apache.maven.artifact.Artifact> resolvedArtifacts) throws InvalidVersionSpecificationException {
        Map<MavenDependencyAdapterMapping, org.apache.maven.artifact.Artifact> matchedArtifact = kouplelessAdapterConfig
            .matches(resolvedArtifacts);
        for (Entry<MavenDependencyAdapterMapping, org.apache.maven.artifact.Artifact> entry : matchedArtifact
            .entrySet()) {
            MavenDependencyMatcher matcher = entry.getKey().getMatcher();
            getLog().info("koupleless adapter matched artifact: " + entry.getValue()
                          + " with matcher: " + matcher);
        }
        return matchedArtifact.keySet().stream().map(MavenDependencyAdapterMapping::getAdapter)
            .collect(Collectors.toList());
    }

    void addDependenciesDynamically() throws Exception {
        if (kouplelessAdapterConfig == null) {
            getLog().info("kouplelessAdapterConfig is null, skip adding dependencies.");
            return;
        }

        Collection<Dependency> dependencies = getDependenciesToAdd();
        for (Dependency dependency : dependencies) {
            try {
                Preconditions.checkArgument(StringUtils.isNotEmpty(dependency.getVersion()),
                    "dependency version is empty: " + dependency);
                Preconditions.checkArgument(StringUtils.isNotEmpty(dependency.getGroupId()),
                    "dependency groupId is empty: " + dependency);

                getLog().debug("start downloading dependency: " + dependency);
                Artifact artifact = downloadDependency(dependency);
                getLog().debug("start add dependency to project root: " + dependency);
                addArtifactToProjectRoot(artifact);
                getLog().info("success add dependency: " + dependency);
            } catch (Throwable t) {
                getLog().error("error add dependency: " + dependency.toString(), t);
                throw new RuntimeException(t);
            }
        }
    }

    public Artifact downloadDependency(Dependency dependency) {
        DefaultArtifact patchArtifact = new DefaultArtifact(
            dependency.getGroupId() + ":" + dependency.getArtifactId() + ":"
                                                            + dependency.getVersion());

        try {
            ArtifactRequest artifactRequest = new ArtifactRequest().setArtifact(patchArtifact)
                .setRepositories(project.getRemoteProjectRepositories());

            ArtifactResult artifactResult = repositorySystem
                .resolveArtifact(session.getRepositorySession(), artifactRequest);

            Preconditions.checkState(artifactResult.isResolved(), "artifact not resolved.");
            return artifactResult.getArtifact();
        } catch (Throwable t) {
            getLog().error(t);
            throw new RuntimeException(t);
        }
    }

    void addArtifactToProjectRoot(Artifact artifact) {
        File file = artifact.getFile();
        File buildDir = new File(outputDirectory, "classes");
        Map<String, Byte[]> entryToContent = JarFileUtils.getFileContentAsLines(file,
            Pattern.compile("(.*\\.class$|^META-INF/services/.*$|^META-INF/spring.factories$)"));

        for (Map.Entry<String, Byte[]> entry : entryToContent.entrySet()) {
            adapterCopyService.copy(buildDir, entry.getKey(),
                ArrayUtils.toPrimitive(entry.getValue()));
        }

    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws MojoExecutionException {
        try {
            initKouplelessAdapterConfig();
            addDependenciesDynamically();
        } catch (Throwable t) {
            getLog().error(t);
            throw new RuntimeException(t);
        }
    }
}

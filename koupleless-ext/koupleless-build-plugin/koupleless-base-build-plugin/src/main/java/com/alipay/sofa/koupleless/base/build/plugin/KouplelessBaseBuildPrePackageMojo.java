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
import com.alipay.sofa.koupleless.base.build.plugin.adapter.ClassCopyStrategy;
import com.alipay.sofa.koupleless.base.build.plugin.adapter.MergeServiceDirectoryCopyStrategy;
import com.alipay.sofa.koupleless.base.build.plugin.adapter.MergeSpringFactoryConfigCopyStrategy;
import com.alipay.sofa.koupleless.base.build.plugin.adapter.PatchCopyStrategy;
import com.alipay.sofa.koupleless.base.build.plugin.common.JarFileUtils;
import com.alipay.sofa.koupleless.base.build.plugin.model.CompositeKouplelessAdapterConfig;
import com.alipay.sofa.koupleless.base.build.plugin.model.MavenDependencyAdapterMapping;
import com.alipay.sofa.koupleless.base.build.plugin.model.MavenDependencyMatcher;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.compiler.CompilerMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.ArtifactType;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.version.InvalidVersionSpecificationException;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.maven.plugin.compiler.CompilerMojo;

/**
 * Goal which touches a timestamp file.
 *
 * @author zzl_i
 * @version 1.0.0
 */
@Mojo(name = "add-patch", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class KouplelessBaseBuildPrePackageMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    File                                         outputDirectory;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    public MavenProject                          project;

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    public MavenSession                          session;

    @Parameter(defaultValue = "${project.basedir}", required = true)
    public File                                  baseDir;

    @Component
    public RepositorySystem                      repositorySystem;

    protected CompositeKouplelessAdapterConfig   kouplelessAdapterConfig;

    @Parameter(defaultValue = "", required = false)
    public String                                kouplelessAdapterConfigVersion;

    @Parameter(defaultValue = "", required = false)
    public List<Dependency>                      customAdaptorMappingDependencies;

    AdapterCopyService                           adapterCopyService                   = new AdapterCopyService();

    private ClassCopyStrategy                    classCopyStrategy                    = new ClassCopyStrategy();
    private MergeServiceDirectoryCopyStrategy    mergeServiceDirectoryCopyStrategy    = new MergeServiceDirectoryCopyStrategy();
    private MergeSpringFactoryConfigCopyStrategy mergeSpringFactoryConfigCopyStrategy = new MergeSpringFactoryConfigCopyStrategy();
    private PatchCopyStrategy                    patchCopyStrategy                    = new PatchCopyStrategy();

    String                                       defaultGroupId                       = "";
    String                                       defaultVersion                       = "";

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
    List<MavenDependencyAdapterMapping> getDependenciesToAdd() throws Exception {
        List<MavenDependencyAdapterMapping> adapterDependencies = new ArrayList<>();
        if (kouplelessAdapterConfig == null) {
            getLog().info("kouplelessAdapterConfig is null, skip adding dependencies.");
            return adapterDependencies;
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

    List<MavenDependencyAdapterMapping> getDependenciesByMatching(Collection<org.apache.maven.artifact.Artifact> resolvedArtifacts) throws InvalidVersionSpecificationException {
        Map<MavenDependencyAdapterMapping, org.apache.maven.artifact.Artifact> matchedArtifact = kouplelessAdapterConfig
            .matches(resolvedArtifacts);
        for (Entry<MavenDependencyAdapterMapping, org.apache.maven.artifact.Artifact> entry : matchedArtifact
            .entrySet()) {
            MavenDependencyMatcher matcher = entry.getKey().getMatcher();
            getLog().info("koupleless adapter matched artifact: " + entry.getValue()
                          + " with matcher: " + matcher);
        }

        List<MavenDependencyAdapterMapping> result = new ArrayList<>();
        for (Map.Entry<MavenDependencyAdapterMapping, org.apache.maven.artifact.Artifact> entry : matchedArtifact
            .entrySet()) {
            MavenDependencyAdapterMapping mapping = entry.getKey();
            org.apache.maven.artifact.Artifact artifact = entry.getValue();
            Dependency sourceDep = new Dependency();
            sourceDep.setGroupId(artifact.getGroupId());
            sourceDep.setArtifactId(artifact.getArtifactId());
            sourceDep.setVersion(artifact.getVersion());
            mapping.setSourceToAdapter(sourceDep);
            result.add(mapping);
        }
        return result;
    }

    void addDependenciesDynamically() throws Exception {
        if (kouplelessAdapterConfig == null) {
            getLog().info("kouplelessAdapterConfig is null, skip adding dependencies.");
            return;
        }

        Collection<MavenDependencyAdapterMapping> dependencyMappings = getDependenciesToAdd();
        for (MavenDependencyAdapterMapping adapterMapping : dependencyMappings) {
            Dependency sourceDep = adapterMapping.getSourceToAdapter();
            Dependency dependency = adapterMapping.getAdapter();
            try {
                Preconditions.checkArgument(StringUtils.isNotEmpty(dependency.getVersion()),
                    "dependency version is empty: " + dependency);
                Preconditions.checkArgument(StringUtils.isNotEmpty(dependency.getGroupId()),
                    "dependency groupId is empty: " + dependency);

                getLog().debug("start add dependency to project root: " + dependency);
                addArtifactToProjectRoot(sourceDep, dependency);
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
            throw new RuntimeException(t);
        }
    }

    public Artifact downloadSourceDependency(Dependency dependency) {
        DefaultArtifact patchArtifact = new DefaultArtifact(dependency.getGroupId(),
            dependency.getArtifactId(), "sources", "jar", dependency.getVersion());

        try {
            ArtifactRequest artifactRequest = new ArtifactRequest().setArtifact(patchArtifact)
                .setRepositories(project.getRemoteProjectRepositories());

            ArtifactResult artifactResult = repositorySystem
                .resolveArtifact(session.getRepositorySession(), artifactRequest);

            Preconditions.checkState(artifactResult.isResolved(), "artifact not resolved.");
            return artifactResult.getArtifact();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SneakyThrows
    void addArtifactToProjectRoot(Dependency sourceDep, Dependency desDep) {
        Artifact sourceArtifact = downloadDependency(sourceDep);
        Artifact desArtifact = downloadDependency(desDep);
        File file = desArtifact.getFile();
        File buildDir = new File(outputDirectory, "classes");

        // add patched source to compile source root
        File toBuildSourceDir = new File(outputDirectory, "source");
        //        this.getCompileSourceRoots().add(toBuildSourceDir.getAbsolutePath());

        Map<String, Byte[]> sourceEntryToConent = new HashMap<>();
        Map<String, Byte[]> entryToContent = JarFileUtils.getFileContentAsLines(file,
            Pattern.compile(
                "(.*\\.class$|.*\\.patch$|^META-INF/services/.*$|^META-INF/spring.factories$)"));

        boolean isPatch = false;
        for (Map.Entry<String, Byte[]> entry : entryToContent.entrySet()) {
            if (StringUtils.endsWith(entry.getKey(), ".patch")) {
                isPatch = true;
                break;
            }
        }
        if (isPatch) {
            File sourceFile = sourceArtifact.getFile();
            // 读取任何文件
            sourceEntryToConent = JarFileUtils.getFileContentAsLines(sourceFile,
                Pattern.compile(".*"));
        }

        for (Map.Entry<String, Byte[]> entry : entryToContent.entrySet()) {
            String entryName = entry.getKey();
            byte[] content = ArrayUtils.toPrimitive(entry.getValue());

            if (entryName.endsWith(".class")) {
                classCopyStrategy.copy(buildDir, entryName, content);
            } else if (entryName.startsWith("META-INF/services")) {
                mergeServiceDirectoryCopyStrategy.copy(buildDir, entryName, content);
            } else if (entryName.equals("META-INF/spring.factories")) {
                mergeSpringFactoryConfigCopyStrategy.copy(buildDir, entryName, content);
            } else if (entryName.endsWith(".patch")) {
                // 1. 读取 source 文件
                // 2. 读取 patch 文件
                // 3. 执行 patch
                String sourceEntryName = entryName.replace(".patch", "");
                if (sourceEntryToConent.containsKey(sourceEntryName)) {
                    InputStreamReader sourceOutputReader = new InputStreamReader(
                        new ByteArrayInputStream(
                            ArrayUtils.toPrimitive(sourceEntryToConent.get(sourceEntryName))));
                    List<String> sourceOriginalLines = IOUtils.readLines(sourceOutputReader);

                    InputStreamReader patchOutputReader = new InputStreamReader(
                        new ByteArrayInputStream(content));
                    List<String> patchLines = IOUtils.readLines(patchOutputReader);
                    Patch patch = UnifiedDiffUtils.parseUnifiedDiff(patchLines);
                    List<String> patchedSource = DiffUtils.patch(sourceOriginalLines, patch);
                    classCopyStrategy.copy(toBuildSourceDir, sourceEntryName,
                        patchedSource.stream().collect(Collectors.joining("\n")).getBytes());
                }
            }
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

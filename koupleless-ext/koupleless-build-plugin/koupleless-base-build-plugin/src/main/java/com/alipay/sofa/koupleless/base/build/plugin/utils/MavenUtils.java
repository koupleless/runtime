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
package com.alipay.sofa.koupleless.base.build.plugin.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.MojoExecutionException;
import com.alipay.sofa.koupleless.base.build.plugin.model.ArtifactItem;
import org.apache.maven.plugins.dependency.resolvers.ListMojo;
import org.apache.maven.plugins.dependency.utils.DependencyStatusSets;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.STRING_COLON;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: MavenUtils.java, v 0.1 2024年07月15日 13:58 立蓬 Exp $
 */
public class MavenUtils {

    private static MavenXpp3Reader reader = new MavenXpp3Reader();
    private static MavenXpp3Writer writer = new MavenXpp3Writer();

    public static MavenProject getRootProject(MavenProject project) {
        if (project == null) {
            return null;
        }
        MavenProject parent = project;
        while (parent.hasParent() && parent.getParent().getBasedir() != null) {
            parent = parent.getParent();
        }
        return parent;
    }

    public static Set<ArtifactItem> getAllBundleArtifact(MavenProject project) {
        File basedir = getRootProject(project).getBasedir();
        return getBundlesArtifact(basedir.getAbsolutePath());
    }

    public static Dependency createDependency(Artifact artifact) {
        Dependency d = new Dependency();
        d.setArtifactId(artifact.getArtifactId());
        // baseVersion is the version in pom, not the version parsed by maven repository
        // e.g. if a dependency is set as 1.0.0-SNAPSHOT, the baseVersion is 1.0.0-SNAPSHOT, but the version maybe 1.0.0-20240805.013141-59
        d.setVersion(artifact.getBaseVersion());
        d.setGroupId(artifact.getGroupId());
        if (!"jar".equals(artifact.getType())) {
            d.setType(artifact.getType());
        }
        if (artifact.hasClassifier()) {
            d.setClassifier(artifact.getClassifier());
        }
        return d;
    }

    private static Set<ArtifactItem> getBundlesArtifact(String bundlePath) {
        Set<ArtifactItem> results = new HashSet<>();
        Model pom = buildPomModel(getPomFileOfBundle(bundlePath));
        results.add(buildArtifact(pom));
        for (String moduleRelativePath : pom.getModules()) {
            String moduleAbsPath = StringUtils.joinWith(File.separator, bundlePath,
                moduleRelativePath);
            results.addAll(getBundlesArtifact(moduleAbsPath));
        }
        return results;
    }

    private static ArtifactItem buildArtifact(Model model) {
        String groupId = model.getGroupId() == null ? model.getParent().getGroupId()
            : model.getGroupId();
        String version = model.getVersion() == null ? model.getParent().getVersion()
            : model.getVersion();
        String type = model.getPackaging() == null ? "jar" : model.getPackaging();
        return ArtifactItem.builder().groupId(groupId).artifactId(model.getArtifactId())
            .version(version).type(type).build();
    }

    public static File getPomFileOfBundle(String bundlePath) {
        return new File(bundlePath, "pom.xml");
    }

    public static File getPomFileOfBundle(File bundle) {
        return new File(bundle, "pom.xml");
    }

    public static Model buildPomModel(String filePath) {
        return buildPomModel(new File(filePath));
    }

    public static Model buildPomModel(File file) {
        if (!file.exists()) {
            throw new RuntimeException("ERROR, MavenUtils:buildPomModel 文件不存在" + file.getPath());
        }

        try (FileInputStream inputStream = new FileInputStream(file)) {
            return reader.read(inputStream);
        } catch (IOException | XmlPullParserException e) {
            throw new RuntimeException(
                "ERROR, MavenPomUtil:buildPomModel for" + file.getPath() + "\nException:" + e);
        }
    }

    public static Model buildPomModel(InputStream inputStream) {
        try {
            return reader.read(inputStream);
        } catch (IOException | XmlPullParserException e) {
            throw new RuntimeException("ERROR, MavenPomUtil:buildPomModel" + e);
        }
    }

    public static void writePomModel(File file, Model model) {
        try (FileWriter fileWriter = new FileWriter(file)) {
            writer.write(fileWriter, model);
        } catch (IOException e) {
            throw new RuntimeException(
                "ERROR, MavenPomUtil:buildPomModel for" + file.getPath() + "\nException:" + e);
        }
    }

    public static InvocationResult invoke(MavenSession mavenSession, String goal,
                                          File pomFile) throws MavenInvocationException,
                                                        MojoExecutionException {

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(pomFile);
        List<String> goals = new ArrayList<>(Collections.singletonList(goal));
        Properties userProperties = mavenSession.getCurrentProject().getProjectBuildingRequest()
            .getUserProperties();
        if (userProperties != null) {
            userProperties.forEach((key, value) -> goals.add(String.format("-D%s=%s", key, value)));
        }
        request.setGoals(goals);
        request.setBatchMode(mavenSession.getSettings().getInteractiveMode());
        request.setProfiles(mavenSession.getSettings().getActiveProfiles());
        setSettingsLocation(mavenSession, request);
        Invoker invoker = new DefaultInvoker();
        return invoker.execute(request);

    }

    private static void setSettingsLocation(MavenSession mavenSession, InvocationRequest request) {
        File userSettingsFile = mavenSession.getRequest().getUserSettingsFile();
        if (userSettingsFile != null && userSettingsFile.exists()) {
            request.setUserSettingsFile(userSettingsFile);
        }
        File globalSettingsFile = mavenSession.getRequest().getGlobalSettingsFile();
        if (globalSettingsFile != null && globalSettingsFile.exists()) {
            request.setGlobalSettingsFile(globalSettingsFile);
        }
    }

    public static Set<Artifact> getDependencyArtifacts(MavenProject mavenProject) throws MojoExecutionException {
        DependencyListMojo mojo = new DependencyListMojo(mavenProject);
        DependencyStatusSets statusSets = mojo.getDependencySets();
        return statusSets.getResolvedDependencies();
    }

    public static String getDependencyIdentity(Dependency dependency) {
        if (StringUtils.isNotEmpty(dependency.getClassifier())) {
            return dependency.getGroupId() + STRING_COLON + dependency.getArtifactId()
                   + STRING_COLON + dependency.getVersion() + STRING_COLON
                   + dependency.getClassifier() + STRING_COLON + dependency.getType();
        } else {
            return dependency.getGroupId() + STRING_COLON + dependency.getArtifactId()
                   + STRING_COLON + dependency.getVersion() + STRING_COLON + dependency.getType();
        }
    }

    public static String getArtifactIdentity(Artifact artifact) {
        if (artifact.hasClassifier()) {
            return artifact.getGroupId() + STRING_COLON + artifact.getArtifactId() + STRING_COLON
                   + artifact.getVersion() + STRING_COLON + artifact.getClassifier() + STRING_COLON
                   + artifact.getType();
        } else {
            return artifact.getGroupId() + STRING_COLON + artifact.getArtifactId() + STRING_COLON
                   + artifact.getVersion() + STRING_COLON + artifact.getType();
        }

    }

    private static class DependencyListMojo extends ListMojo {
        MavenProject mavenProject;

        DependencyListMojo(MavenProject project) {
            mavenProject = project;
        }

        public DependencyStatusSets getDependencySets() throws MojoExecutionException {
            return super.getDependencySets(false);
        }

        @Override
        public MavenProject getProject() {
            return this.mavenProject;
        }
    }

}
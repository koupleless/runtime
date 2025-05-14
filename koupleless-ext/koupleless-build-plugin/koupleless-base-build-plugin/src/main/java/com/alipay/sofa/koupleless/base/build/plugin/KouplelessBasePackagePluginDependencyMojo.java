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

import com.alipay.sofa.koupleless.base.build.plugin.model.ArtifactItem;
import com.alipay.sofa.koupleless.base.build.plugin.utils.MavenUtils;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.STRING_COLON;
import static com.alipay.sofa.koupleless.base.build.plugin.utils.CollectionUtils.nonNull;
import static com.alipay.sofa.koupleless.base.build.plugin.utils.FileUtils.createNewDirectory;
import static com.alipay.sofa.koupleless.base.build.plugin.utils.MavenUtils.getAllBundleArtifact;
import static com.alipay.sofa.koupleless.base.build.plugin.utils.MavenUtils.getDependencyArtifacts;
import static com.alipay.sofa.koupleless.base.build.plugin.utils.MavenUtils.getPomFileOfBundle;
import static com.alipay.sofa.koupleless.base.build.plugin.utils.MavenUtils.getRootProject;
import static com.alipay.sofa.koupleless.base.build.plugin.utils.MavenUtils.invoke;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: KouplelessBasePackageDependencyMojo.java, v 0.1 2024年07月15日 11:59 立蓬 Exp $
 */
@Mojo(name = "packagePluginDependency", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class KouplelessBasePackagePluginDependencyMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject mavenProject;

    @Component
    private MavenSession mavenSession;

    @Parameter(defaultValue = "${project.basedir}", required = true)
    private File         baseDir;

    @Parameter(defaultValue = "${project.version}")
    private String       pluginDependencyVersion;

    @Parameter(defaultValue = "pluginDependencyArtifact")
    private String       pluginDependencyArtifactId;

    @Parameter(defaultValue = "${project.groupId}", required = true)
    private String       pluginDependencyGroupId;

    /**
     * 基座依赖标识，以 ${groupId}:${artifactId}:${version} 或 ${groupId}:${artifactId} 标识
     */
    @Parameter(defaultValue = "")
    private String       baseDependencyParentIdentity;

    private File         pluginDependencyRootDir;

    @Parameter(defaultValue = "true")
    private String       cleanAfterPackagePluginDependencies;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            checkBaseDependencyParentIdentity();

            createPluginDependencies();
        } catch (Throwable t) {
            getLog().error(t);
            throw new RuntimeException(t);
        } finally {
            clearDependencyRootDir();
        }
    }

    private void createPluginDependencies() throws Exception {
        createDependencyRootDir();
        integratePluginDependencies();
        installPluginDependencies();
        moveToOutputs();
    }

    private void checkBaseDependencyParentIdentity() throws MojoExecutionException {
        if (null == getBaseDependencyParentOriginalModel()) {
            throw new MojoExecutionException(String
                .format("can not find base dependency parent: %s", baseDependencyParentIdentity));
        }
    }

    private void moveToOutputs() throws IOException {
        File outputsDir = new File(baseDir, "outputs");
        createNewDirectory(outputsDir);

        File newPomFile = new File(outputsDir, "pom.xml");
        Files.copy(getPomFileOfBundle(pluginDependencyRootDir).toPath(), newPomFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING);
        getLog().info("copy pom.xml to " + newPomFile.getAbsolutePath() + " success.");
    }

    protected void installPluginDependencies() throws MojoExecutionException,
                                               MavenInvocationException {
        try {
            InvocationResult result = invoke(mavenSession, "install",
                getPomFileOfBundle(pluginDependencyRootDir));
            if (result.getExitCode() != 0) {
                throw new MojoExecutionException("execute mvn install failed for base dependencies",
                    result.getExecutionException());
            }
        } catch (Exception e) {
            getLog().error("execute mvn install failed for base dependencies", e);
            throw e;
        }
        getLog().info("package base dependencies success.");
    }

    private void createDependencyRootDir() throws IOException {
        //0. 创建一个空maven工程
        File rootDir = new File(baseDir, pluginDependencyArtifactId);
        createNewDirectory(rootDir);

        File facadePom = new File(rootDir, "pom.xml");
        if (!facadePom.exists()) {
            facadePom.createNewFile();
        }
        getLog().info("create plugin dependency directory success." + rootDir.getAbsolutePath());
        pluginDependencyRootDir = rootDir;
    }

    private void integratePluginDependencies() throws MojoExecutionException, IOException {

        Model pom = new Model();
        // 设置 xml 头
        pom.setModelEncoding("UTF-8");
        pom.setVersion("1.0");
        pom.setModelVersion("4.0.0");

        // 设置 parent
        MavenProject rootProject = getRootProject(mavenProject);
        pom.setParent(rootProject.getOriginalModel().getParent());

        // 设置 groupId, artifactId, version
        pom.setGroupId(pluginDependencyGroupId);
        pom.setArtifactId(pluginDependencyArtifactId);
        pom.setVersion(pluginDependencyVersion);
        pom.setPackaging("pom");

        // 配置 license
        License license = new License();
        license.setName("The Apache License, Version 2.0");
        license.setUrl("http://www.apache.org/licenses/LICENSE-2.0.txt");
        pom.setLicenses(Collections.singletonList(license));

        // 配置 properties
        Properties properties = new Properties();
        properties.putAll(this.mavenProject.getOriginalModel().getProperties());
        properties.putIfAbsent("maven-source-plugin.version", "3.2.1");
        pom.setProperties(properties);

        // 配置 dependencyManagement
        Set<ArtifactItem> baseModuleArtifacts = getAllBundleArtifact(this.mavenProject);
        getLog().info("find maven module of base: " + baseModuleArtifacts);
        Set<String> baseDependencyIdentitiesWithoutVersion = getAllBaseDependencies().stream()
            .map(MavenUtils::getDependencyIdentityWithoutVersion).collect(Collectors.toSet());
        Map<String, Dependency> resolvedProjectDependencyManagement = getResolvedProjectDependencyManagement();
        Set<Artifact> dependencyArtifacts = getDependencyArtifacts(this.mavenProject);
        DependencyManagement dependencyManagement = new DependencyManagement();
        List<Dependency> dependencies = nonNull(dependencyArtifacts).stream()
            // 过滤掉 scope 是 provided, test, system 的依赖，因为只考虑插件给业务模块提供的依赖
            .filter(artifact -> !"provided".equals(artifact.getScope())
                                && !"test".equals(artifact.getScope())
                                && !"system".equals(artifact.getScope()))
            // 过滤掉基座的依赖
            .filter(artifact -> !baseDependencyIdentitiesWithoutVersion
                .contains(MavenUtils.getArtifactIdentityWithoutVersion(artifact)))
            // 过滤出不属于项目的依赖
            .filter(artifact -> baseModuleArtifacts.stream().noneMatch(
                baseModule -> Objects.equals(baseModule.getGroupId(), artifact.getGroupId())
                              && Objects.equals(baseModule.getArtifactId(),
                                  artifact.getArtifactId())))
            // 转换为 dependency
            .map(artifact -> {
                // 如果项目的依赖管理中有该依赖，则复用项目的依赖管理
                if (resolvedProjectDependencyManagement
                    .containsKey(MavenUtils.getArtifactIdentity(artifact))) {
                    return resolvedProjectDependencyManagement
                        .get(MavenUtils.getArtifactIdentity(artifact)).clone();
                }
                return MavenUtils.createDependency(artifact);
            }).collect(Collectors.toList());
        dependencyManagement.setDependencies(dependencies);
        pom.setDependencyManagement(dependencyManagement);

        // 配置 build
        Model baseDependencyPomTemplate = MavenUtils.buildPomModel(this.getClass().getClassLoader()
            .getResourceAsStream("base-dependency-pom-template.xml"));
        Build build = baseDependencyPomTemplate.getBuild().clone();
        pom.setBuild(build);

        MavenUtils.writePomModel(getPomFileOfBundle(pluginDependencyRootDir), pom);
    }

    private List<Dependency> getAllBaseDependencies() {
        // 获取基座DependencyParent的原始Model
        Model baseDependencyPom = getBaseDependencyParentOriginalModel();

        if (null == baseDependencyPom.getDependencyManagement()) {
            return Collections.emptyList();
        }

        return baseDependencyPom.getDependencyManagement().getDependencies();
    }

    protected Model getBaseDependencyParentOriginalModel() {
        MavenProject proj = mavenProject;
        while (null != proj) {
            if (getGAIdentity(proj.getArtifact()).equals(baseDependencyParentIdentity)
                || getGAVIdentity(proj.getArtifact()).equals(baseDependencyParentIdentity)) {
                return proj.getOriginalModel();
            }
            proj = proj.getParent();
        }
        return null;
    }

    private String getGAVIdentity(Artifact artifact) {
        return artifact.getGroupId() + STRING_COLON + artifact.getArtifactId() + STRING_COLON
               + artifact.getBaseVersion();
    }

    private String getGAIdentity(Artifact artifact) {
        return artifact.getGroupId() + STRING_COLON + artifact.getArtifactId();
    }

    protected Map<String, Dependency> getResolvedProjectDependencyManagement() {
        if (null == this.mavenProject.getDependencyManagement()) {
            return Collections.emptyMap();
        }
        return this.mavenProject.getDependencyManagement().getDependencies().stream()
            .collect(Collectors.toMap(MavenUtils::getDependencyIdentity, d -> d));
    }

    private void clearDependencyRootDir() {
        if (Boolean.parseBoolean(cleanAfterPackagePluginDependencies)
            && pluginDependencyRootDir != null) {
            FileUtils.deleteQuietly(pluginDependencyRootDir);
        }
    }

}

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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.alipay.sofa.koupleless.base.build.plugin.utils.CollectionUtils.nonNull;
import static com.alipay.sofa.koupleless.base.build.plugin.utils.MavenUtils.getDependencyArtifacts;
import static com.alipay.sofa.koupleless.base.build.plugin.utils.MavenUtils.getModuleArtifactIds;
import static com.alipay.sofa.koupleless.base.build.plugin.utils.MavenUtils.getRootProject;
import static com.alipay.sofa.koupleless.base.build.plugin.utils.MavenUtils.invoke;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: KouplelessBasePackageDependencyMojo.java, v 0.1 2024年07月15日 11:59 立蓬 Exp $
 */
@Mojo(name = "packageDependency", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class KouplelessBasePackageDependencyMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject mavenProject;

    @Component
    private MavenSession mavenSession;

    @Parameter(defaultValue = "${project.basedir}", required = true)
    private File         baseDir;

    @Parameter(defaultValue = "${project.version}")
    private String       version;

    @Parameter(defaultValue = "artifact")
    private String       artifactId;

    @Parameter(defaultValue = "${project.groupId}", required = true)
    private String       groupId;

    private File         dependencyRootDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            createBaseDependency();
        } catch (Throwable t) {
            getLog().error(t);
            throw new RuntimeException(t);
        } finally {
            clearDependencyRootDir();
        }
    }

    private void createBaseDependency() throws Exception {
        createDependencyRootDir();
        integrateBaseDependencies();
        installBaseDependencies();
    }

    private void installBaseDependencies() {
        try {
            InvocationResult result = invoke(mavenSession, "install", getBaseDependenciesPom());
            if (result.getExitCode() != 0) {
                throw new MojoExecutionException("execute mvn install failed for base dependencies",
                    result.getExecutionException());
            }
        } catch (Exception e) {
            getLog().error("execute mvn install failed for base dependencies", e);
        }
        getLog().info("package base dependencies success.");
    }

    private void createDependencyRootDir() throws IOException {
        //0. 创建一个空maven工程
        File facadeRootDir = new File(baseDir, artifactId);
        if (facadeRootDir.exists()) {
            FileUtils.deleteQuietly(facadeRootDir);
        }
        if (!facadeRootDir.exists()) {
            facadeRootDir.mkdirs();
        }

        File facadePom = new File(facadeRootDir, "pom.xml");
        if (!facadePom.exists()) {
            facadePom.createNewFile();
        }
        getLog()
            .info("create base dependency directory success." + facadeRootDir.getAbsolutePath());
        dependencyRootDir = facadeRootDir;
    }

    private void integrateBaseDependencies() throws MojoExecutionException {
        Model pom = new Model();
        // 设置 xml 头
        pom.setModelEncoding("UTF-8");
        pom.setVersion("1.0");
        pom.setModelVersion("4.0.0");

        // 设置 parent
        MavenProject rootProject = getRootProject(mavenProject);
        pom.setParent(rootProject.getOriginalModel().getParent());

        // 设置 groupId, artifactId, version
        pom.setGroupId(groupId);
        pom.setArtifactId(artifactId);
        pom.setVersion(version);
        pom.setPackaging("pom");

        // 配置 license
        License license = new License();
        license.setName("The Apache License, Version 2.0");
        license.setUrl("http://www.apache.org/licenses/LICENSE-2.0.txt");
        pom.setLicenses(Collections.singletonList(license));

        // 配置 dependencyManagement
        Set<String> baseModuleArtifactIds = getModuleArtifactIds(this.mavenProject);
        getLog().info("find maven module of base: " + baseModuleArtifactIds);
        Set<Artifact> dependencyArtifacts = getDependencyArtifacts(this.mavenProject);
        DependencyManagement dependencyManagement = new DependencyManagement();
        List<Dependency> dependencies = nonNull(dependencyArtifacts).stream()
            .filter(it -> !baseModuleArtifactIds.contains(it.getArtifactId())).map(it -> {
                Dependency d = new Dependency();
                d.setArtifactId(it.getArtifactId());
                d.setVersion(it.getVersion());
                d.setGroupId(it.getGroupId());
                if (!"jar".equals(it.getType())) {
                    d.setType(it.getType());
                }
                if(it.hasClassifier()){
                    d.setClassifier(it.getClassifier());
                }
                return d;
            }).collect(Collectors.toList());
        dependencyManagement.setDependencies(dependencies);
        pom.setDependencyManagement(dependencyManagement);

        // 配置 build
        Model baseDependencyPomTemplate = MavenUtils.buildPomModel(this.getClass().getClassLoader()
            .getResourceAsStream("base-dependency-pom-template.xml"));
        Build build = baseDependencyPomTemplate.getBuild().clone();
        pom.setBuild(build);

        MavenUtils.writePomModel(getBaseDependenciesPom(), pom);
    }

    private void clearDependencyRootDir() {
        FileUtils.deleteQuietly(dependencyRootDir);
    }

    private File getBaseDependenciesPom() {
        return new File(dependencyRootDir, "pom.xml");
    }
}
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

import com.alipay.sofa.koupleless.base.build.plugin.constant.Constants;
import com.alipay.sofa.koupleless.base.build.plugin.model.ArtifactItem;
import com.alipay.sofa.koupleless.base.build.plugin.utils.MavenUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
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
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.alipay.sofa.koupleless.base.build.plugin.KouplelessBasePackageFacadeMojo.JVMFileTypeEnum.JAVA;
import static com.alipay.sofa.koupleless.base.build.plugin.KouplelessBasePackageFacadeMojo.JVMFileTypeEnum.KOTLIN;
import static com.alipay.sofa.koupleless.base.build.plugin.utils.FileUtils.createNewDirectory;
import static com.alipay.sofa.koupleless.base.build.plugin.utils.CollectionUtils.nonNull;
import static com.alipay.sofa.koupleless.base.build.plugin.utils.MavenUtils.getAllBundleArtifact;
import static com.alipay.sofa.koupleless.base.build.plugin.utils.MavenUtils.getDependencyArtifacts;
import static com.alipay.sofa.koupleless.base.build.plugin.utils.MavenUtils.getPomFileOfBundle;
import static com.alipay.sofa.koupleless.base.build.plugin.utils.MavenUtils.getRootProject;
import static com.alipay.sofa.koupleless.base.build.plugin.utils.MavenUtils.invoke;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: KouplelessBasePackageFacadeMojo.java, v 0.1 2024年07月15日 11:58 立蓬 Exp $
 */
@Mojo(name = "packageFacade", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class KouplelessBasePackageFacadeMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject                       mavenProject;

    @Component
    private MavenSession                       mavenSession;

    @Parameter(defaultValue = "${project.basedir}", required = true)
    private File                               baseDir;

    @Parameter(defaultValue = "${project.version}")
    private String                             facadeVersion;

    @Parameter(defaultValue = "facadeArtifact")
    private String                             facadeArtifactId;

    @Parameter(defaultValue = "${project.groupId}", required = true)
    private String                             facadeGroupId;

    @Parameter(defaultValue = "")
    private LinkedHashSet<String>              jvmFiles                  = new LinkedHashSet<>();

    @Parameter(defaultValue = "true")
    private String                             cleanAfterPackageFacade;

    @Parameter(defaultValue = "1.8")
    private String                             jvmTarget;

    private static final List<JVMFileTypeEnum> SUPPORT_FILE_TYPE_TO_COPY = Stream.of(JAVA, KOTLIN)
        .collect(Collectors.toList());

    private File                               facadeRootDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            createBaseFacade();
        } catch (Throwable t) {
            getLog().error(t);
            throw new RuntimeException(t);
        } finally {
            clearFacadeRootDir();
        }
    }

    private void createBaseFacade() throws Exception {
        createFacadeRootDir();
        extractBaseFacades();
        installBaseFacades();
        moveToOutputs();
    }

    protected void moveToOutputs() throws IOException {
        File outputsDir = new File(baseDir, "outputs");
        createNewDirectory(outputsDir);

        File facadeTargetDir = new File(facadeRootDir, "target");
        if (!facadeTargetDir.exists()) {
            throw new RuntimeException(
                "facade target dir not exists: " + facadeTargetDir.getAbsolutePath());
        }
        File[] targetFiles = facadeTargetDir.listFiles();
        for (File f : targetFiles) {
            if (f.getName().endsWith(".jar")) {
                File newFile = new File(outputsDir, f.getName());
                Files.copy(f.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                getLog().info("copy " + f.getAbsolutePath() + " to " + newFile.getAbsolutePath()
                              + " success.");
            }
        }
        File newPomFile = new File(outputsDir, "pom.xml");
        Files.copy(getPomFileOfBundle(facadeRootDir).toPath(), newPomFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING);
        getLog().info("copy pom.xml to " + newPomFile.getAbsolutePath() + " success.");
    }

    protected void extractBaseFacades() throws Exception {
        //1. 复制指定的jvm文件到该module
        copyMatchedJVMFiles();
        getLog().info("copy supported jvm files success.");

        //2. 配置pom
        configPom();
    }

    private void configPom() throws MojoExecutionException {
        Model pom = new Model();
        // 设置 xml 头
        pom.setModelEncoding("UTF-8");
        pom.setVersion("1.0");
        pom.setModelVersion("4.0.0");

        // 设置 groupId, artifactId, version
        pom.setGroupId(facadeGroupId);
        pom.setArtifactId(facadeArtifactId);
        pom.setVersion(facadeVersion);

        // 配置 license
        License license = new License();
        license.setName("The Apache License, Version 2.0");
        license.setUrl("http://www.apache.org/licenses/LICENSE-2.0.txt");
        pom.setLicenses(Collections.singletonList(license));

        // 配置 properties
        Properties properties = this.mavenProject.getProperties();
        properties.putIfAbsent("maven-source-plugin.version", "3.2.1");
        pom.setProperties(properties);

        // 配置依赖，全都设置成 provided
        Set<ArtifactItem> baseModuleArtifacts = getAllBundleArtifact(this.mavenProject);
        getLog().info("find maven module of base: " + baseModuleArtifacts);
        Set<Artifact> dependencyArtifacts = getDependencyArtifacts(this.mavenProject);
        List<Dependency> dependencies = nonNull(dependencyArtifacts).stream()
            // 过滤出不属于项目的依赖
            .filter(d -> baseModuleArtifacts.stream().noneMatch(
                baseModule -> Objects.equals(baseModule.getGroupId(), d.getGroupId())
                              && Objects.equals(baseModule.getArtifactId(), d.getArtifactId())))
            // 过滤出 scope 不是 test, system 的依赖
            .filter(d -> !"test".equals(d.getScope()) && !"system".equals(d.getScope())).map(d -> {
                Dependency res = MavenUtils.createDependency(d);
                res.setScope("provided");
                Exclusion exclusion = new Exclusion();
                exclusion.setArtifactId("*");
                exclusion.setGroupId("*");
                res.setExclusions(Collections.singletonList(exclusion));
                return res;
            }).collect(Collectors.toList());
        pom.setDependencies(dependencies);

        // 配置 build
        Model baseFacadePomTemplate = MavenUtils.buildPomModel(
            this.getClass().getClassLoader().getResourceAsStream("base-facade-pom-template.xml"));
        Build build = baseFacadePomTemplate.getBuild().clone();
        pom.setBuild(build);

        // 配置 maven-compiler-plugin 中的 jdk 版本
        Plugin mavenCompilerPlugin = build.getPlugins().stream()
            .filter(it -> it.getArtifactId().equals("maven-compiler-plugin")).findFirst().get();
        Xpp3Dom mavenCompilerConfig = (Xpp3Dom) mavenCompilerPlugin.getConfiguration();
        mavenCompilerConfig.getChild("source").setValue(jvmTarget);
        mavenCompilerConfig.getChild("target").setValue(jvmTarget);

        // 配置 kotlin-maven-plugin 中的 jdk 版本
        Plugin kotlinMavenPlugin = build.getPlugins().stream()
            .filter(it -> it.getArtifactId().equals("kotlin-maven-plugin")).findFirst().get();
        Xpp3Dom kotlinMavenConfig = (Xpp3Dom) kotlinMavenPlugin.getConfiguration();
        kotlinMavenConfig.getChild("jvmTarget").setValue(jvmTarget);

        MavenUtils.writePomModel(getPomFileOfBundle(facadeRootDir), pom);
    }

    private void copyMatchedJVMFiles() throws IOException {
        List<File> allSupportedJVMFiles = getSupportedJVMFiles(
            getRootProject(this.mavenProject).getBasedir());

        for (File file : allSupportedJVMFiles) {
            JVMFileTypeEnum type = getMatchedType(file);
            String fullClassName = type.parseFullClassName(file);
            if (shouldCopy(fullClassName)) {
                File newFile = new File(facadeRootDir.getAbsolutePath() + File.separator
                                        + type.parseRelativePath(file));
                if (!newFile.getParentFile().exists()) {
                    newFile.getParentFile().mkdirs();
                }

                Files.copy(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                getLog().info("copy file from " + file.getAbsolutePath() + " to "
                              + newFile.getAbsolutePath() + " success.");
            }
        }
    }

    protected static List<File> getSupportedJVMFiles(File baseDir) {
        List<File> supportedJVMFiles = new ArrayList<>();
        getSupportedJVMFiles(baseDir, supportedJVMFiles);
        return supportedJVMFiles;
    }

    private static void getSupportedJVMFiles(File baseDir, List<File> supportedJVMFiles) {
        File[] files = baseDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                getSupportedJVMFiles(file, supportedJVMFiles);
            } else if (null != getMatchedType(file)) {
                supportedJVMFiles.add(file);
            }
        }
    }

    private static JVMFileTypeEnum getMatchedType(File file) {
        for (JVMFileTypeEnum type : SUPPORT_FILE_TYPE_TO_COPY) {
            if (type.matches(file)) {
                return type;
            }
        }
        return null;
    }

    private boolean shouldCopy(String fullClassName) {
        for (String classPattern : jvmFiles) {
            if (classPattern.equals(fullClassName)) {
                return true;
            }

            if (classPattern.endsWith(Constants.PACKAGE_PREFIX_MARK)) {
                classPattern = StringUtils.removeEnd(classPattern, Constants.PACKAGE_PREFIX_MARK);
                if (fullClassName.startsWith(classPattern)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void installBaseFacades() throws MojoExecutionException, MavenInvocationException {
        try {
            InvocationResult result = invoke(mavenSession, "install",
                getPomFileOfBundle(facadeRootDir));
            if (result.getExitCode() != 0) {
                throw new MojoExecutionException("execute mvn install failed for base facades",
                    result.getExecutionException());
            }
        } catch (Exception e) {
            getLog().error("execute mvn install failed for base facades", e);
            throw e;
        }
        getLog().info("package base facades success.");
    }

    protected void createFacadeRootDir() throws IOException {
        //0. 创建一个空maven工程
        File rootDir = new File(baseDir, facadeArtifactId);
        createNewDirectory(rootDir);

        File facadePom = new File(rootDir, "pom.xml");
        if (!facadePom.exists()) {
            facadePom.createNewFile();
        }
        getLog().info("create base dependency directory success." + rootDir.getAbsolutePath());
        facadeRootDir = rootDir;
    }

    protected void clearFacadeRootDir() {
        if (Boolean.parseBoolean(cleanAfterPackageFacade) && facadeRootDir != null) {
            FileUtils.deleteQuietly(facadeRootDir);
        }
    }

    enum JVMFileTypeEnum {
                          JAVA("java", ".java", StringUtils.join(new String[] { "src", "main",
                                                                                "java" },
                              File.separator) + File.separator), KOTLIN("kotlin", ".kt",
                                                                        StringUtils.join(
                                                                            new String[] { "src",
                                                                                           "main",
                                                                                           "kotlin" },
                                                                            File.separator)
                                                                                         + File.separator);

        private String name;
        private String suffix;
        private String parentRootDir;

        JVMFileTypeEnum(String name, String suffix, String parentRootDir) {
            this.name = name;
            this.suffix = suffix;
            this.parentRootDir = parentRootDir;
        }

        public boolean matches(File file) {
            String absPath = file.getAbsolutePath();
            boolean inParentRootDir = absPath.contains(parentRootDir);
            boolean onlyOneParentRootDir = absPath.indexOf(parentRootDir) == absPath
                .lastIndexOf(parentRootDir);
            boolean matchedType = absPath.endsWith(suffix);
            return inParentRootDir && onlyOneParentRootDir && matchedType;
        }

        public String parseFullClassName(File file) {
            if (!matches(file)) {
                return null;
            }
            String absPath = file.getAbsolutePath();
            return StringUtils.removeEnd(StringUtils.substringAfter(absPath, parentRootDir), suffix)
                .replace(File.separator, ".");
        }

        public String parseRelativePath(File file) {
            if (!matches(file)) {
                return null;
            }
            String absPath = file.getAbsolutePath();
            return parentRootDir + StringUtils.substringAfter(absPath, parentRootDir);
        }
    }
}

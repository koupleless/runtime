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
package com.alipay.sofa.koupleless.base.build.plugin.model;

import com.alipay.sofa.koupleless.base.build.plugin.KouplelessBaseBuildPrePackageMojo;
import com.alipay.sofa.koupleless.base.build.plugin.common.JarFileUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.version.Version;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.ARK_CONF_BASE_DIR;
import static com.alipay.sofa.koupleless.base.build.plugin.utils.MavenUtils.getDependencyIdentity;
import static com.google.common.collect.Maps.newHashMap;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: CompositeKouplelessAdapterConfig.java, v 0.1 2024年11月26日 17:47 立蓬 Exp $
 */
public class CompositeKouplelessAdapterConfig implements AdapterConfig {

    private static final String           CUSTOM_MAPPING_FILE    = "adapter-mapping.yaml";

    private static final String           REMOTE_ADAPTER_CONFIGS = "com.alipay.sofa.koupleless:koupleless-adapter-configs";

    private static final String           DEFAULT_MAPPING_FILE   = "adapter-mapping-default.yaml";

    /**
     * 用户自定义配置：用户配置的 ${baseDir}/adapter-mapping.yaml
     */
    private KouplelessAdapterConfig       customConfig;

    /**
     * 远程配置：读取最新的 koupleless-adapter-configs 中的配置
     */
    private List<KouplelessAdapterConfig> remoteConfigs          = new ArrayList<>();

    /**
     * 插件默认配置：如果远程配置不存在，则使用插件默认配置
     */
    private KouplelessAdapterConfig       defaultConfig;

    public CompositeKouplelessAdapterConfig(KouplelessBaseBuildPrePackageMojo mojo) {
        initCustomConfig(mojo);

        initRemoteConfig(mojo);

        initDefaultConfig();
    }

    /**
     * priority：custom > remote > default
     * @return java.util.List<org.apache.maven.model.Dependency>
     */
    @Override
    public List<Dependency> getCommonDependencies() {
        Map<String, Dependency> custom = Maps.newHashMap();
        if (null != customConfig) {
            custom = toMapWithIdAsKey(customConfig.getCommonDependencies());
        }

        Map<String, Dependency> remote = Maps.newHashMap();
        for (KouplelessAdapterConfig config : remoteConfigs) {
            remote.putAll(toMapWithIdAsKey(config.getCommonDependencies()));
        }

        Map<String, Dependency> defaulted = Maps.newHashMap();
        if (null != defaultConfig) {
            defaulted = toMapWithIdAsKey(defaultConfig.getCommonDependencies());
        }

        // 优先级：custom > remote > defaulted
        Map<String, Dependency> res = newHashMap();
        res.putAll(defaulted);
        res.putAll(remote);
        res.putAll(custom);
        return new ArrayList<>(res.values());
    }

    private Map<String, Dependency> toMapWithIdAsKey(List<Dependency> dependencies) {
        if (CollectionUtils.isEmpty(dependencies)) {
            return Collections.emptyMap();
        }

        Map<String, Dependency> res = newHashMap();
        dependencies.forEach(d -> {
            String key = getDependencyIdentity(d);
            res.put(key, d);
        });
        return res;
    }

    /**
     * no priority, all configs are activated
     * @param resolvedArtifacts project resolved artifacts
     * @return java.util.Map<com.alipay.sofa.koupleless.base.build.plugin.model.MavenDependencyAdapterMapping,org.apache.maven.artifact.Artifact>
     */
    @Override
    public Map<MavenDependencyAdapterMapping, org.apache.maven.artifact.Artifact> matches(Collection<org.apache.maven.artifact.Artifact> resolvedArtifacts) {
        Map<MavenDependencyAdapterMapping, org.apache.maven.artifact.Artifact> custom = newHashMap();
        if (null != customConfig) {
            custom = customConfig.matches(resolvedArtifacts);
        }

        Map<MavenDependencyAdapterMapping, org.apache.maven.artifact.Artifact> remote = newHashMap();
        for (KouplelessAdapterConfig config : remoteConfigs) {
            remote.putAll(config.matches(resolvedArtifacts));
        }

        Map<MavenDependencyAdapterMapping, org.apache.maven.artifact.Artifact> defaulted = newHashMap();
        if (null != defaultConfig) {
            defaulted = defaultConfig.matches(resolvedArtifacts);
        }

        Map<MavenDependencyAdapterMapping, org.apache.maven.artifact.Artifact> res = newHashMap();
        res.putAll(defaulted);
        res.putAll(remote);
        res.putAll(custom);
        return res;
    }

    @SneakyThrows
    private void initCustomConfig(KouplelessBaseBuildPrePackageMojo mojo) {
        File configFile = FileUtils.getFile(mojo.baseDir, ARK_CONF_BASE_DIR, CUSTOM_MAPPING_FILE);
        if (!configFile.exists()) {
            mojo.getLog().info(String.format(
                "koupleless-base-build-plugin: custom-adapter-mapping-config %s not found, will not config it",
                configFile.getPath()));
            return;
        }

        InputStream mappingConfigIS = new FileInputStream(configFile);
        Yaml yaml = new Yaml();
        customConfig = yaml.loadAs(mappingConfigIS, KouplelessAdapterConfig.class);
    }

    private void initDefaultConfig() {
        // 如果远程配置存在，则无需初始化插件默认配置
        if (CollectionUtils.isNotEmpty(remoteConfigs)) {
            return;
        }

        InputStream mappingConfigIS = this.getClass().getClassLoader()
            .getResourceAsStream(DEFAULT_MAPPING_FILE);

        Yaml yaml = new Yaml();
        defaultConfig = yaml.loadAs(mappingConfigIS, KouplelessAdapterConfig.class);
    }

    private void initRemoteConfig(KouplelessBaseBuildPrePackageMojo mojo) {
        String kouplelessAdapterConfigVersion = parseRemoteConfigVersion(mojo);
        Artifact artifact = downloadAdapterConfigsJar(mojo, kouplelessAdapterConfigVersion);
        remoteConfigs = parseConfigs(artifact);
    }

    private Artifact downloadAdapterConfigsJar(KouplelessBaseBuildPrePackageMojo mojo,
                                               String kouplelessAdapterConfigVersion) {
        if (StringUtils.isEmpty(kouplelessAdapterConfigVersion)) {
            return null;
        }

        try {
            return mojo.downloadDependency(
                parseDependency(REMOTE_ADAPTER_CONFIGS + ":" + kouplelessAdapterConfigVersion));
        } catch (Exception e) {
            mojo.getLog()
                .warn("Failed to resolve koupleless-adapter-configs, use default config only.");
        }
        return null;
    }

    private List<KouplelessAdapterConfig> parseConfigs(Artifact artifact) {
        if (null == artifact) {
            return Collections.emptyList();
        }

        File file = artifact.getFile();
        Map<String, Byte[]> entryToContent = JarFileUtils.getFileContentAsLines(file,
            Pattern.compile("(.*\\.yaml)"));

        for (Map.Entry<String, Byte[]> entry : entryToContent.entrySet()) {
            InputStream inputStream = new ByteArrayInputStream(
                ArrayUtils.toPrimitive(entry.getValue()));
            Yaml yaml = new Yaml();
            KouplelessAdapterConfig config = yaml.loadAs(inputStream,
                KouplelessAdapterConfig.class);
            remoteConfigs.add(config);
        }
        return remoteConfigs;
    }

    private String parseRemoteConfigVersion(KouplelessBaseBuildPrePackageMojo mojo) {
        if (StringUtils.isNotEmpty(mojo.kouplelessAdapterConfigVersion)) {
            return mojo.kouplelessAdapterConfigVersion;
        }

        // 从远端获取最新版本
        String version = "";
        try {
            VersionRangeRequest rangeRequest = new VersionRangeRequest()
                .setArtifact(new org.eclipse.aether.artifact.DefaultArtifact(
                    REMOTE_ADAPTER_CONFIGS + ":(,)"))
                .setRepositories(mojo.project.getRemoteProjectRepositories());

            VersionRangeResult rangeResult = mojo.repositorySystem
                .resolveVersionRange(mojo.session.getRepositorySession(), rangeRequest);
            Version latestVersion = rangeResult.getHighestVersion();
            version = latestVersion.toString();
        } catch (VersionRangeResolutionException e) {
            mojo.getLog().warn(
                "Failed to resolve latest version of koupleless-adapter-configs, use default config only.");
        }

        return version;
    }

    private Dependency parseDependency(String dependencyStr) {
        String[] dependencyParts = StringUtils.split(dependencyStr, ":");

        Preconditions.checkState(dependencyParts.length == 3,
            "Invalid dependency format: " + dependencyStr);

        Dependency d = new Dependency();
        d.setGroupId(dependencyParts[0]);
        d.setArtifactId(dependencyParts[1]);
        d.setVersion(dependencyParts[2]);
        return d;
    }
}
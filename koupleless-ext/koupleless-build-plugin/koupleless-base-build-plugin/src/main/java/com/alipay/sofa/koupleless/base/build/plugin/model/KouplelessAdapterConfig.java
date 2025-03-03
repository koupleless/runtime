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

import lombok.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.InvalidVersionSpecificationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.alipay.sofa.koupleless.base.build.plugin.utils.ParseUtils.parseVersion;
import static com.alipay.sofa.koupleless.base.build.plugin.utils.ParseUtils.parseVersionRange;

/**
 * <p>KouplelessAdapterConfig class.</p>
 *
 * @author CodeNoobKing
 * @since 2024/2/6
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KouplelessAdapterConfig implements AdapterConfig {
    String                              version;                               // 依赖包的版本。

    /**
     * 存在一些通用的依赖，需要用户引入。
     */
    @Builder.Default
    List<Dependency>                    commonDependencies = new ArrayList<>();

    /**
     * 适配的依赖。
     */
    @Builder.Default
    List<MavenDependencyAdapterMapping> adapterMappings    = new ArrayList<>();

    @Override
    public Map<MavenDependencyAdapterMapping, Artifact> matches(Collection<Artifact> resolvedArtifacts) {
        Map<MavenDependencyAdapterMapping, Artifact> adapterMatches = new HashMap<>();
        if (CollectionUtils.isEmpty(adapterMappings)) {
            return adapterMatches;
        }

        for (org.apache.maven.artifact.Artifact artifact : resolvedArtifacts) {
            for (MavenDependencyAdapterMapping adapterMapping : adapterMappings) {
                MavenDependencyMatcher matcher = adapterMapping.getMatcher();
                if (matches(matcher, artifact)) {
                    // use the default version if not configured
                    Dependency adapter = adapterMapping.getAdapter();
                    if (StringUtils.isEmpty(adapter.getVersion())) {
                        adapter.setVersion(version);
                    }

                    adapterMatches.put(adapterMapping, artifact);
                }
            }
        }
        return adapterMatches;
    }

    private boolean matches(MavenDependencyMatcher matcher, Artifact artifact) {
        if (null == matcher) {
            return false;
        }

        // match with regexp
        if (regexpMatches(matcher, artifact)) {
            return true;
        }

        // match with versionRange
        return versionRangeMatches(matcher, artifact);
    }

    private boolean regexpMatches(MavenDependencyMatcher matcher,
                                  org.apache.maven.artifact.Artifact artifact) {
        if (null == matcher || null == matcher.getRegexp()) {
            return false;
        }

        String regexp = matcher.getRegexp();
        String dependencyId = getArtifactFullId(artifact);
        return Pattern.compile(regexp).matcher(dependencyId).matches();
    }

    private boolean versionRangeMatches(MavenDependencyMatcher matcher,
                                        org.apache.maven.artifact.Artifact artifact) {
        if (null == matcher || null == matcher.getUnionVersionRange()) {
            return false;
        }

        return StringUtils.equals(matcher.getGroupId(), artifact.getGroupId())
               && StringUtils.equals(matcher.getArtifactId(), artifact.getArtifactId()) && matcher
                   .getUnionVersionRange().containsVersion(parseVersion(artifact.getVersion()));
    }

    private String getArtifactFullId(org.apache.maven.artifact.Artifact artifact) {
        return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":"
               + artifact.getBaseVersion() + ":" + artifact.getType()
               + (StringUtils.isNotEmpty(artifact.getClassifier()) ? ":" + artifact.getClassifier()
                   : "");
    }
}

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

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.InvalidVersionSpecificationException;
import org.eclipse.aether.version.VersionRange;

/**
 * <p>MavenDependencyMatcher class.</p>
 *
 * @author CodeNoobKing
 * @since 2024/2/6
 * @version 1.0.0
 */
@NoArgsConstructor
public class MavenDependencyMatcher {
    /**
     * 用正则表达式匹配用户的依赖。
     */
    @Getter
    @Setter
    private String                     regexp;

    /**
     * 依赖的groupId，如：org.springframework.boot
     */
    @Getter
    @Setter
    private String                     groupId;

    /**
     * 依赖的artifactId，如：spring-boot
     */
    @Getter
    @Setter
    private String                     artifactId;

    /**
     * 适配的版本范围，比如：
     * [1.0,2.0) 表示从 1.0（包含）到 2.0（不包含）的版本。
     * [1.0,2.0] 表示从 1.0（包含）到 2.0（包含）的版本。
     * (,1.0] 表示小于或等于 1.0 的版本。
     * [2.0,) 表示大于或等于 2.0 的版本。
     */
    @Getter
    private String                     versionRange;

    @Getter
    private VersionRange               genericVersionRange;

    private final GenericVersionScheme versionScheme = new GenericVersionScheme();

    @Builder
    public MavenDependencyMatcher(String regexp, String groupId, String artifactId,
                                  String versionRange) throws InvalidVersionSpecificationException {
        this.regexp = regexp;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.versionRange = versionRange;
        this.genericVersionRange = initGenericVersionRange();
    }

    public void setVersionRange(String versionRange) throws InvalidVersionSpecificationException {
        this.versionRange = versionRange;
        this.genericVersionRange = initGenericVersionRange();
    }

    private VersionRange initGenericVersionRange() throws InvalidVersionSpecificationException {
        if (StringUtils.isEmpty(versionRange)) {
            return null;
        }

        return versionScheme.parseVersionRange(versionRange);
    }
}

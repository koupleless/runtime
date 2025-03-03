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

import com.alipay.sofa.koupleless.base.build.plugin.utils.MavenUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.aether.util.version.UnionVersionRange;
import org.eclipse.aether.version.InvalidVersionSpecificationException;
import org.eclipse.aether.version.VersionRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.alipay.sofa.koupleless.base.build.plugin.utils.ParseUtils.parseVersionRange;

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
    @Deprecated
    private String       regexp;

    /**
     * 依赖的groupId，如：org.springframework.boot
     */
    @Getter
    @Setter
    private String       groupId;

    /**
     * 依赖的artifactId，如：spring-boot
     */
    @Getter
    @Setter
    private String       artifactId;

    /**
     * 适配的版本范围，比如：
     * [1.0,2.0) 表示从 1.0（包含）到 2.0（不包含）的版本。
     * [1.0,2.0] 表示从 1.0（包含）到 2.0（包含）的版本。
     * (,1.0] 表示小于或等于 1.0 的版本。
     * [2.0,) 表示大于或等于 2.0 的版本。
     * [2.11.0, 2.13.1),(2.13.1, 2.19.0] 表示[2.11.0, 2.13.1)和(2.13.1, 2.19.0]的并集，即：从 2.11.0（包含）到 2.13.1（不包含）或 2.13.1（不包含）到 2.19.0（包含）的版本。
     */
    @Getter
    private String       versionRange;

    @Getter
    private VersionRange unionVersionRange;

    @Builder
    public MavenDependencyMatcher(String regexp, String groupId, String artifactId,
                                  String versionRange) throws InvalidVersionSpecificationException {
        this.regexp = regexp;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.versionRange = versionRange;
        this.unionVersionRange = initUnionVersionRange();
    }

    public void setVersionRange(String versionRange) throws InvalidVersionSpecificationException {
        this.versionRange = versionRange;
        this.unionVersionRange = initUnionVersionRange();
    }

    private VersionRange initGenericVersionRange(String versionRange) throws InvalidVersionSpecificationException {
        if (StringUtils.isEmpty(versionRange)) {
            return null;
        }

        return parseVersionRange(versionRange);
    }

    private VersionRange initUnionVersionRange() throws InvalidVersionSpecificationException {
        if (StringUtils.isEmpty(versionRange)) {
            return null;
        }

        List<String> strVersionRanges = MavenUtils.parseUnionVersionRange(versionRange);
        List<VersionRange> versionRanges = new ArrayList<>();
        for (String s : strVersionRanges) {
            versionRanges.add(initGenericVersionRange(s));
        }
        return UnionVersionRange.from(versionRanges);
    }

    @Override
    public String toString() {
        return String.format(
            "MavenDependencyMatcher{regexp=%s,groupId=%s,artifactId=%s,versionRange=%s}", regexp,
            groupId, artifactId, versionRange);
    }
}

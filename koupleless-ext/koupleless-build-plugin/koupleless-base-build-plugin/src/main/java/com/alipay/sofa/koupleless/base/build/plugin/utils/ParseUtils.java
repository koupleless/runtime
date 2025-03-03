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
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.util.version.UnionVersionRange;
import org.eclipse.aether.version.Version;
import org.eclipse.aether.version.VersionRange;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.alipay.sofa.koupleless.base.build.plugin.constant.Constants.COMMA_SPLIT;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: CommonUtils.java, v 0.1 2024年06月26日 13:16 立蓬 Exp $
 */
public class ParseUtils {
    private static final GenericVersionScheme versionScheme = new GenericVersionScheme();

    public static Set<String> getSetValues(Properties prop, String confKey) {
        if (null == prop) {
            return newHashSet();
        }
        String[] values = StringUtils.split(prop.getProperty(confKey), COMMA_SPLIT);
        return values == null ? newHashSet() : newHashSet(values);
    }

    public static Set<String> getStringSet(Map<String, Object> yaml, String confKey) {
        if (null != yaml && yaml.containsKey(confKey) && null != yaml.get(confKey)) {
            return newHashSet((List<String>) yaml.get(confKey));
        }
        return newHashSet();
    }

    public static VersionRange parseVersionRange(String versionRange) {
        try {
            return versionScheme.parseVersionRange(versionRange);
        } catch (Exception e) {
            throw new RuntimeException("parse version range failed, version range: " + versionRange,
                e);
        }
    }

    public static Version parseVersion(String version) {
        try {
            return versionScheme.parseVersion(version);
        } catch (Exception e) {
            throw new RuntimeException("parse version failed, version: " + version, e);
        }
    }
}

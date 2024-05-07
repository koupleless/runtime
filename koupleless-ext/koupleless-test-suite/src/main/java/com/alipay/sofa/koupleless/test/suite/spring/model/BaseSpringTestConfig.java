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
package com.alipay.sofa.koupleless.test.suite.spring.model;

import com.alipay.sofa.ark.loader.jar.JarUtils;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>BaseSpringTestConfig class.</p>
 *
 * @author CodeNoobKing
 * @since 2024/3/11
 * @version 1.0.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BaseSpringTestConfig {

    private Class<?>     mainClass;

    private String       artifactId;

    @Builder.Default
    private List<String> excludeArtifactIds = new ArrayList<>();

    /**
     * <p>init.</p>
     */
    public void init() {
        Preconditions.checkState(mainClass != null, "mainClass must not be blank");
        if (StringUtils.isBlank(artifactId)) {
            URL location = mainClass.getProtectionDomain().getCodeSource().getLocation();
            Preconditions.checkNotNull(location, "Code source location must not be null");
            artifactId = JarUtils.parseArtifactId(location.toString());
        }
    }
}

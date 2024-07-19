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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ArtifactItem.java, v 0.1 2024年07月17日 18:10 立蓬 Exp $
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ArtifactItem {
    private static final String GAV_SPLIT = ":";

    private String              groupId;

    private String              artifactId;

    private String              version;

    private String              classifier;

    @Builder.Default
    private String              type      = "jar";

    @Builder.Default
    private String              scope     = "compile";

    @Override
    public int hashCode() {
        return Objects.hash(this.groupId, this.artifactId, this.type, this.version,
            this.classifier);
    }
}
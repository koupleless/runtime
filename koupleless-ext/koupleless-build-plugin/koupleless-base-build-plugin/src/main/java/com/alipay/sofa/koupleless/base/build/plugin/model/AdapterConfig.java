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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.eclipse.aether.version.InvalidVersionSpecificationException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: AdapterConfig.java, v 0.1 2024年11月27日 11:40 立蓬 Exp $
 */
public interface AdapterConfig {
    /**
     * a mapping rule only map to an artifact; but an artifact can be mapped to multiple mapping rules
     * mapping -> artifact: 1 -> 1
     * artifact -> mapping: 1 -> N
     *
     */
    Map<MavenDependencyAdapterMapping, Artifact> matches(Collection<Artifact> resolvedArtifacts);

    List<Dependency> getCommonDependencies();
}
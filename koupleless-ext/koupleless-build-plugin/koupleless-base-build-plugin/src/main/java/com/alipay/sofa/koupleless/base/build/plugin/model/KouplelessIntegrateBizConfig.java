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

import java.util.HashSet;
import java.util.Set;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: KouplelessIntegrateBizConfig.java, v 0.1 2024年06月25日 12:03 立蓬 Exp $
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class KouplelessIntegrateBizConfig {
    /**
     * 指定文件URL，将拷贝到 classpath 下的 SOFA-ARK/biz，支持：file:///,http://, https://
     */
    Set<String> fileURLs  = new HashSet<>();

    /**
     * 指定本地目录，将拷贝该目录下的所有 ark-biz 到 classpath 下的 SOFA-ARK/biz
     */
    Set<String> localDirs = new HashSet<>();

    public void addFileURLs(Set<String> urls) {
        fileURLs.addAll(urls);
    }

    public void addLocalDirs(Set<String> absolutePath) {
        localDirs.addAll(absolutePath);
    }
}
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
package com.alipay.sofa.koupleless.test.suite.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <p>CompatibleTestBizConfig class.</p>
 *
 * @author CodeNoobKing
 * @since 2024/1/15
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompatibleTestBizConfig {
    /**
     * 当前测试的名字。
     */
    private String       name;

    /**
     * 初始化的类名。
     */
    private String       bootstrapClass;

    /**
     * 测试的类列表。
     */
    private List<String> testClasses;

    /**
     * 需要被模块加载的类列表。
     */
    private List<String> loadByBizClassLoaderPatterns;

}

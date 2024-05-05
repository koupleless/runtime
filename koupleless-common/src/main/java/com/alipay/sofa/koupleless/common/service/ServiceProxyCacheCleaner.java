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
package com.alipay.sofa.koupleless.common.service;

import com.alipay.sofa.koupleless.common.BizRuntimeContext;
import com.alipay.sofa.koupleless.common.BizRuntimeContextRegistry;

import java.util.Set;

/**
 * <p>ServiceProxyCacheCleaner class.</p>
 *
 * @author yuanyuan
 * @author zzl_i
 * @since 2023/9/25 11:52 下午
 * @version 1.0.0
 */
public class ServiceProxyCacheCleaner {

    /**
     * <p>clean.</p>
     *
     * @param classLoader a {@link java.lang.ClassLoader} object
     */
    public static void clean(ClassLoader classLoader) {
        Set<BizRuntimeContext> runtimeSet = BizRuntimeContextRegistry.getRuntimeSet();
        for (BizRuntimeContext bizRuntimeContext : runtimeSet) {
            bizRuntimeContext.removeServiceProxyCaches(classLoader);
        }
    }
}

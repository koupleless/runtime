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
package com.alipay.sofa.koupleless.arklet.core.hook.base;

import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.koupleless.arklet.core.common.model.BaseMetadata;

import java.util.UUID;

/**
 * <p>BaseMetadataHookImpl.</p>
 *
 * @author dongnan
 * @since 2024/09/03
 * @version 1.0.0
 */
public class BaseMetadataHookImpl implements BaseMetadataHook {

    final String defaultNameEnvKey    = "koupleless.arklet.metadata.name";
    final String defaultVersionEnvKey = "koupleless.arklet.metadata.version";
    final String defaultEnvKey        = "koupleless.arklet.metadata.env";

    final UUID   baseID               = UUID.randomUUID();

    private String getName() {
        return EnvironmentUtils.getProperty(defaultNameEnvKey);
    }

    private String getVersion() {
        return EnvironmentUtils.getProperty(defaultVersionEnvKey);
    }

    @Override
    public BaseMetadata getBaseMetadata() {
        return BaseMetadata.builder().name(getName()).version(getVersion()).build();
    }

    @Override
    public String getRuntimeEnv() {
        return EnvironmentUtils.getProperty(defaultEnvKey);
    }

    @Override
    public String getBaseID() {
        return baseID.toString();
    }
}

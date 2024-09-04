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
package com.alipay.sofa.koupleless.arklet.core.spi.metadata;

import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.koupleless.arklet.core.common.model.Constants;
import com.alipay.sofa.koupleless.arklet.core.common.model.InstallRequest;
import com.alipay.sofa.koupleless.arklet.core.common.model.Metadata;

import static com.alipay.sofa.koupleless.arklet.core.common.model.Constants.STRATEGY_UNINSTALL_THEN_INSTALL;

/**
 * <p>MetadataHook interface.</p>
 *
 * @author dongnan
 * @since 2024/09/03
 * @version 1.0.0
 */
public class MetadataHookImpl implements MetadataHook {

    final String defaultNameEnvKey    = "koupleless.arklet.metadata.name";
    final String defaultVersionEnvKey = "koupleless.arklet.metadata.version";
    final String defaultEnvKey        = "koupleless.arklet.metadata.env";

    private String getName() {
        return EnvironmentUtils.getProperty(defaultNameEnvKey);
    }

    private String getVersion() {
        return EnvironmentUtils.getProperty(defaultVersionEnvKey);
    }

    @Override
    public Metadata getMetadata() {
        return Metadata.builder().name(getName()).version(getVersion()).build();
    }

    @Override
    public String getEnv() {
        return EnvironmentUtils.getProperty(defaultEnvKey);
    }
}

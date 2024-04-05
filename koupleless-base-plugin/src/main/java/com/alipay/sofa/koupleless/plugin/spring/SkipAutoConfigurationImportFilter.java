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
package com.alipay.sofa.koupleless.plugin.spring;

import com.alipay.sofa.koupleless.common.BizRuntimeContext;
import com.alipay.sofa.koupleless.common.BizRuntimeContextRegistry;
import com.alipay.sofa.koupleless.common.util.ArkUtils;
import com.alipay.sofa.koupleless.common.util.PropertiesUtil;
import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Set;

public class SkipAutoConfigurationImportFilter implements AutoConfigurationImportFilter,
                                               EnvironmentAware {

    private static final String MODULE_AUTO_CONFIGURATION_EXCLUDE = "koupleless.module.autoconfigure.exclude";
    private static final String MODULE_AUTO_CONFIGURATION_INCLUDE = "koupleless.module.autoconfigure.include";

    private Environment         environment;

    @Override
    public boolean[] match(String[] autoConfigurationClasses,
                           AutoConfigurationMetadata autoConfigurationMetadata) {
        if (autoConfigurationClasses == null) {
            return new boolean[0];
        }

        boolean[] result = new boolean[autoConfigurationClasses.length];
        if (!ArkUtils.isModuleBiz()) {
            Arrays.fill(result, true);
            return result;
        }

        // 1. getting the exclude and include configs from base
        BizRuntimeContext bizRuntimeContext = BizRuntimeContextRegistry
            .getMasterBizRuntimeContext();
        Set<String> excludeConfigInBase = PropertiesUtil
            .formatPropertyValues(bizRuntimeContext.getRootApplicationContext().getEnvironment()
                .getProperty(MODULE_AUTO_CONFIGURATION_EXCLUDE));
        Set<String> includeConfigInBase = PropertiesUtil
            .formatPropertyValues(bizRuntimeContext.getRootApplicationContext().getEnvironment()
                .getProperty(MODULE_AUTO_CONFIGURATION_INCLUDE));

        // 2. gettting the exclude and include configs from current biz
        Set<String> excludeConfigInBiz = PropertiesUtil
            .formatPropertyValues(this.environment.getProperty(MODULE_AUTO_CONFIGURATION_EXCLUDE));
        Set<String> includeConfigInBiz = PropertiesUtil
            .formatPropertyValues(this.environment.getProperty(MODULE_AUTO_CONFIGURATION_INCLUDE));

        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            result[i] = true;
            String autoConfigurationClass = autoConfigurationClasses[i];
            if (excludeConfigInBase.contains(autoConfigurationClass)) {
                result[i] = false;
            }
            if (includeConfigInBase.contains(autoConfigurationClass)) {
                result[i] = true;
            }

            if (excludeConfigInBiz.contains(autoConfigurationClass)) {
                result[i] = false;
            }
            if (includeConfigInBiz.contains(autoConfigurationClass)) {
                result[i] = true;
            }
        }

        return result;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}

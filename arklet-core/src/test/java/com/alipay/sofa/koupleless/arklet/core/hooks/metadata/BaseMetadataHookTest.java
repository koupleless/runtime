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
package com.alipay.sofa.koupleless.arklet.core.hooks.metadata;

import com.alipay.sofa.koupleless.arklet.core.BaseTest;
import com.alipay.sofa.koupleless.arklet.core.common.model.BaseMetadata;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author dongnan
 * @since 2024/09/04
 */
public class BaseMetadataHookTest extends BaseTest {
    @Test
    public void getBaseMetadata() {
        System.setProperty("koupleless.arklet.metadata.name", "test_metadata_hook");
        System.setProperty("koupleless.arklet.metadata.version", "test_metadata_hook_version");
        BaseMetadata metadata = baseMetadataHook.getBaseMetadata();
        Assert.assertEquals("test_metadata_hook", metadata.getName());
        Assert.assertEquals("test_metadata_hook_version", metadata.getVersion());
    }

    @Test
    public void getRuntimeEnv() {
        System.setProperty("koupleless.arklet.metadata.env", "test_env");
        String env = baseMetadataHook.getRuntimeEnv();
        Assert.assertEquals("test_env", env);
    }
}

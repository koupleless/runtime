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
package com.alipay.sofa.koupleless.arklet.tunnel;

import com.alipay.sofa.koupleless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.koupleless.arklet.core.ops.UnifiedOperationService;
import com.alipay.sofa.koupleless.arklet.core.command.CommandService;
import com.alipay.sofa.koupleless.arklet.core.health.HealthService;
import com.alipay.sofa.koupleless.arklet.core.hook.base.BaseMetadataHook;
import org.junit.BeforeClass;
import org.mockito.Mock;

/**
 * @author mingmen
 * @since 2023/9/5
 */
public class BaseTest {

    @Mock
    public static CommandService          commandService;

    @Mock
    public static UnifiedOperationService operationService;

    @Mock
    public static HealthService           healthService;

    @Mock
    public static BaseMetadataHook        baseMetadataHook;

    @BeforeClass
    public static void setup() throws Exception {
        System.setProperty("koupleless.arklet.metadata.name", "test_metadata_hook");
        System.setProperty("koupleless.arklet.metadata.version", "test_metadata_hook_version");
        System.setProperty("koupleless.arklet.metadata.env", "test_env");
        commandService = ArkletComponentRegistry.getCommandServiceInstance();
        operationService = ArkletComponentRegistry.getOperationServiceInstance();
        healthService = ArkletComponentRegistry.getHealthServiceInstance();
        baseMetadataHook = ArkletComponentRegistry.getApiClientInstance().getMetadataHook();
    }

}

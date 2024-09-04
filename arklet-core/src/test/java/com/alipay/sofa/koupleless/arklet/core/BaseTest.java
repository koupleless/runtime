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
package com.alipay.sofa.koupleless.arklet.core;

import com.alipay.sofa.koupleless.arklet.core.api.ApiClient;
import com.alipay.sofa.koupleless.arklet.core.command.CommandService;
import com.alipay.sofa.koupleless.arklet.core.health.HealthService;
import com.alipay.sofa.koupleless.arklet.core.ops.UnifiedOperationService;
import com.alipay.sofa.koupleless.arklet.core.spi.metadata.MetadataHook;
import org.junit.Before;
import org.mockito.Mock;

/**
 * @author mingmen
 * @since 2023/9/5
 */
public class BaseTest {

    static {
        System.setProperty("koupleless.arklet.custom.tunnel.classname",
            "com.alipay.sofa.koupleless.arklet.core.tunnel.custom.MockTunnel");
    }

    @Mock
    public static CommandService          commandService;

    @Mock
    public static UnifiedOperationService operationService;

    @Mock
    public static HealthService           healthService;

    @Mock
    public static MetadataHook            metadataHook;

    @Before
    public void setup() {
        commandService = ArkletComponentRegistry.getCommandServiceInstance();
        operationService = ArkletComponentRegistry.getOperationServiceInstance();
        healthService = ArkletComponentRegistry.getHealthServiceInstance();
        metadataHook = ArkletComponentRegistry.getApiClientInstance().getMetadataHook();
    }

}

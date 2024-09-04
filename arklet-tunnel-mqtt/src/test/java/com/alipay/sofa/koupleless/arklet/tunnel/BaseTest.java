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
import com.alipay.sofa.koupleless.arklet.core.spi.metadata.MetadataHook;
import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Properties;

/**
 * @author mingmen
 * @since 2023/9/5
 */
public class BaseTest {

    @Mock
    public static Server                  mqttBroker;

    @Mock
    public static final String            BROKER_URL = "tcp://localhost:1883";

    @Mock
    public static MqttClient              mockClient;

    @Mock
    public static CommandService          commandService;

    @Mock
    public static UnifiedOperationService operationService;

    @Mock
    public static HealthService           healthService;

    @Mock
    public static MetadataHook            metadataHook;

    @BeforeClass
    public static void setup() throws Exception {
        System.setProperty("koupleless.arklet.metadata.name", "test_metadata_hook");
        System.setProperty("koupleless.arklet.metadata.version", "test_metadata_hook_version");
        System.setProperty("koupleless.arklet.metadata.env", "test_env");
        commandService = ArkletComponentRegistry.getCommandServiceInstance();
        operationService = ArkletComponentRegistry.getOperationServiceInstance();
        healthService = ArkletComponentRegistry.getHealthServiceInstance();
        metadataHook = ArkletComponentRegistry.getApiClientInstance().getMetadataHook();
        // 启动嵌入式 MQTT Broker
        mqttBroker = startEmbeddedBroker();

        // 使用实际客户端连接到嵌入式 Broker，用于发布消息
        mockClient = new MqttClient(BROKER_URL, "testClient", new MemoryPersistence());
        mockClient.connect();
    }

    @AfterClass
    public static void teardown() {
        // 停止嵌入式 MQTT Broker
        stopEmbeddedBroker(mqttBroker);
    }

    private static Server startEmbeddedBroker() throws Exception {
        Server mqttBroker = new Server();
        Properties configProps = new Properties();
        configProps.setProperty("port", "1883");
        mqttBroker.startServer(new MemoryConfig(configProps));
        return mqttBroker;
    }

    private static void stopEmbeddedBroker(Server mqttBroker) {
        if (mqttBroker != null) {
            mqttBroker.stopServer();
        }
    }
}

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
package com.alipay.sofa.koupleless.arklet.tunnel.paho;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletInitException;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletRuntimeException;
import com.alipay.sofa.koupleless.arklet.core.health.model.Health;
import com.alipay.sofa.koupleless.arklet.tunnel.BaseTest;
import com.alipay.sofa.koupleless.arklet.tunnel.mqtt.paho.MqttMessageHandler;
import com.alipay.sofa.koupleless.arklet.tunnel.mqtt.paho.PahoMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.UUID;

import static org.mockito.Mockito.mockStatic;

/**
 * @author dongnan
 * @since 2024/7/5
 */

public class MqttMessageHandlerTest extends BaseTest {

    @Test(expected = NullPointerException.class)
    public void run() {
        UUID uuid = UUID.randomUUID();
        MqttMessageHandler mqttMessageHandler = new MqttMessageHandler(commandService,
            baseMetadataHook, null, uuid, "test");
        mqttMessageHandler.run();
    }

    @Test(expected = NullPointerException.class)
    public void handleHealthCommand() {
        UUID uuid = UUID.randomUUID();
        MqttMessageHandler mqttMessageHandler = new MqttMessageHandler(commandService,
            baseMetadataHook, null, uuid, "test");
        mqttMessageHandler.handleCommand(BuiltinCommand.HEALTH.getId(),
            new MqttMessage("{}".getBytes()));
    }

    @Test(expected = NullPointerException.class)
    public void handleQueryAllBizCommand() {
        UUID uuid = UUID.randomUUID();
        MqttMessageHandler mqttMessageHandler = new MqttMessageHandler(commandService,
            baseMetadataHook, null, uuid, "test");
        mqttMessageHandler.handleCommand(BuiltinCommand.QUERY_ALL_BIZ.getId(),
            new MqttMessage("{}".getBytes()));
    }

    @Test(expected = ArkletRuntimeException.class)
    public void handleInstallBizCommand() {
        UUID uuid = UUID.randomUUID();
        MqttMessageHandler mqttMessageHandler = new MqttMessageHandler(commandService,
            baseMetadataHook, null, uuid, "test");
        mqttMessageHandler.handleCommand(BuiltinCommand.INSTALL_BIZ.getId(),
            new MqttMessage(
                "{\"bizName\":\"testBiz\", \"bizVersion\":\"0.1.0\", \"bizUrl\":\"testBizUrl\"}"
                    .getBytes()));
    }

    @Test(expected = NullPointerException.class)
    public void handleUnInstallBizCommand() {
        UUID uuid = UUID.randomUUID();
        MqttMessageHandler mqttMessageHandler = new MqttMessageHandler(commandService,
            baseMetadataHook, null, uuid, "test");
        mqttMessageHandler.handleCommand(BuiltinCommand.UNINSTALL_BIZ.getId(),
            new MqttMessage(
                "{\"bizName\":\"testBiz\", \"bizVersion\":\"0.1.0\", \"bizUrl\":\"testBizUrl\"}"
                    .getBytes()));
    }

}
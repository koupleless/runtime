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

import com.alipay.sofa.koupleless.arklet.tunnel.BaseTest;
import com.alipay.sofa.koupleless.arklet.tunnel.mqtt.paho.PahoMqttClient;
import org.eclipse.paho.client.mqttv3.*;
import org.junit.Test;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * @author dongnan
 * @since 2024/7/5
 */

public class PahoClientTest extends BaseTest {

    @Test
    public void openAndClose() throws MqttException {
        UUID uuid = UUID.randomUUID();
        PahoMqttClient client = new PahoMqttClient("localhost", 1883, uuid, "test", "test", "",
            commandService, baseMetadataHook);
        client.open();
        client.close();
    }

    @Test
    public void handleMessage() throws MqttException {
        UUID uuid = UUID.randomUUID();
        PahoMqttClient client = new PahoMqttClient("localhost", 1883, uuid, "test", "test", "",
            commandService, baseMetadataHook);
        client.open();
        // health
        mockClient.publish(
            String.format("koupleless_%s/%s/base/health", baseMetadataHook.getRuntimeEnv(), uuid),
            new MqttMessage("".getBytes()));

        // queryAllBiz
        mockClient.publish(
            String.format("koupleless_%s/%s/base/biz", baseMetadataHook.getRuntimeEnv(), uuid),
            new MqttMessage("".getBytes()));

        // install
        mockClient.publish(String.format("koupleless_%s/%s/base/installBiz",
            baseMetadataHook.getRuntimeEnv(), uuid), new MqttMessage("".getBytes()));
    }

}
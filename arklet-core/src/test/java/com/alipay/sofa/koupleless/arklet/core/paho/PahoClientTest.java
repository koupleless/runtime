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
package com.alipay.sofa.koupleless.arklet.core.paho;

import com.alipay.sofa.koupleless.arklet.core.BaseTest;
import com.alipay.sofa.koupleless.arklet.core.api.tunnel.mqtt.paho.PahoMqttClient;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

/**
 * @author dongnan
 * @since 2024/7/5
 */
public class PahoClientTest extends BaseTest {

    static class HealthMessageListener implements IMqttMessageListener {

        private UUID deviceID;

        HealthMessageListener(UUID deviceID) {
            this.deviceID = deviceID;
        }

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
            System.out.println(topic);
            System.out.println(Arrays.toString(mqttMessage.getPayload()));
            assert topic.equals(String.format("koupleless/%s/base/health", deviceID));
        }
    }

    @Test
    public void command() throws Exception {
        UUID deviceID = UUID.randomUUID();
        PahoMqttClient pahoMqttClient = new PahoMqttClient("broker.emqx.io", 1883, deviceID, "emqx",
            "public", commandService);
        pahoMqttClient.open();

        String broker = "tcp://broker.emqx.io:1883";
        String username = "emqx";
        String password = "public";
        String clientid = "publish_client";
        MqttClient client = new MqttClient(broker, clientid, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        client.connect(options);
        client.publish(String.format("koupleless/%s/health", deviceID),
            new MqttMessage("{}".getBytes()));
        client.subscribe(String.format("koupleless/%s/base/health", deviceID),
            new HealthMessageListener(deviceID));
    }

    @Test
    public void open() throws MqttException {
        PahoMqttClient pahoMqttClient = new PahoMqttClient("broker.emqx.io", 1883,
            UUID.randomUUID(), "emqx", "public", commandService);
        pahoMqttClient.open();
    }
}

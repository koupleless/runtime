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
package com.alipay.sofa.koupleless.arklet.tunnel.mqtt.task;

import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletRuntimeException;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.arklet.core.common.model.BaseMetadata;
import com.alipay.sofa.koupleless.arklet.core.common.model.BaseNetworkInfo;
import com.alipay.sofa.koupleless.arklet.core.hook.base.BaseMetadataHook;
import com.alipay.sofa.koupleless.arklet.core.hook.network.BaseNetworkInfoHook;
import com.alipay.sofa.koupleless.arklet.tunnel.mqtt.model.MqttResponse;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.HashMap;
import java.util.Map;

import static com.alipay.sofa.koupleless.arklet.core.health.model.Constants.LOCAL_HOST_NAME;
import static com.alipay.sofa.koupleless.arklet.core.health.model.Constants.LOCAL_IP;
import static com.alipay.sofa.koupleless.arklet.core.health.model.Constants.NETWORK_INFO;
import static com.alipay.sofa.koupleless.arklet.core.health.model.Constants.STATE;

public class HeartBeatScheduledTask implements Runnable {

    private final String              topic;
    private final MqttClient          mqttClient;
    private final BaseMetadataHook    baseMetadataHook;
    private final BaseNetworkInfoHook baseNetworkInfoHook;

    public HeartBeatScheduledTask(String topic, MqttClient mqttClient,
                                  BaseMetadataHook baseMetadataHook,
                                  BaseNetworkInfoHook baseNetworkInfoHook) {
        this.topic = topic;
        this.mqttClient = mqttClient;
        this.baseMetadataHook = baseMetadataHook;
        this.baseNetworkInfoHook = baseNetworkInfoHook;
    }

    @Override
    public void run() {
        // send heart beat message
        Map<String, Object> heartBeatData = new HashMap<>();

        BaseMetadata metadata = baseMetadataHook.getBaseMetadata();
        heartBeatData.put(
            com.alipay.sofa.koupleless.arklet.core.health.model.Constants.MASTER_BIZ_INFO,
            metadata);

        Map<String, String> networkInfo = new HashMap<>();

        BaseNetworkInfo baseNetworkInfo = baseNetworkInfoHook.getNetworkInfo();

        networkInfo.put(LOCAL_IP, baseNetworkInfo.getLocalIP());
        networkInfo.put(LOCAL_HOST_NAME, baseNetworkInfo.getLocalHostName());

        heartBeatData.put(NETWORK_INFO, networkInfo);
        heartBeatData.put(STATE, BizState.ACTIVATED);

        ArkletLoggerFactory.getDefaultLogger().info("send heart beat message to topic {}: {}",
            topic, heartBeatData);
        try {
            mqttClient.publish(topic,
                JSONObject.toJSONString(MqttResponse.withData(heartBeatData)).getBytes(), 1, false);
        } catch (MqttException e) {
            throw new ArkletRuntimeException("mqtt client publish health status failed", e);
        }
    }
}

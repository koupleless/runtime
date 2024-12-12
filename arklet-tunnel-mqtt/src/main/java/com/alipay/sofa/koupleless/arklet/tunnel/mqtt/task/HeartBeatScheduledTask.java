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
import com.alipay.sofa.koupleless.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.arklet.core.common.model.BaseMetadata;
import com.alipay.sofa.koupleless.arklet.core.common.model.BaseStatus;
import com.alipay.sofa.koupleless.arklet.core.hook.base.BaseMetadataHook;
import com.alipay.sofa.koupleless.arklet.tunnel.mqtt.model.MqttResponse;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class HeartBeatScheduledTask implements Runnable {

    private final String           topic;
    private final MqttClient       mqttClient;
    private final BaseMetadataHook baseMetadataHook;

    public HeartBeatScheduledTask(String topic, MqttClient mqttClient,
                                  BaseMetadataHook baseMetadataHook) {
        this.topic = topic;
        this.mqttClient = mqttClient;
        this.baseMetadataHook = baseMetadataHook;
    }

    @Override
    public void run() {
        // send heart beat message
        BaseMetadata baseMetadata = BaseMetadata.builder().name(baseMetadataHook.getName())
            .identity(baseMetadataHook.getIdentity()).version(baseMetadataHook.getVersion())
            .clusterName(baseMetadataHook.getClusterName()).build();
        BaseStatus baseStatus = BaseStatus.builder().baseMetadata(baseMetadata)
            .localIP(baseMetadataHook.getLocalIP())
            .localHostName(baseMetadataHook.getLocalHostName())
            .state(BizState.ACTIVATED.getBizState()).build();

        ArkletLoggerFactory.getDefaultLogger().info("send heart beat message to topic {}: {}",
            topic, baseStatus);
        try {
            mqttClient.publish(topic,
                JSONObject.toJSONString(MqttResponse.withData(baseStatus)).getBytes(), 1, false);
        } catch (MqttException e) {
            throw new ArkletRuntimeException("mqtt client publish health status failed", e);
        }
    }
}

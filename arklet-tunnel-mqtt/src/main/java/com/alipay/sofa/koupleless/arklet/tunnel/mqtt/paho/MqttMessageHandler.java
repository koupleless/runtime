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
package com.alipay.sofa.koupleless.arklet.tunnel.mqtt.paho;

import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.ark.spi.archive.BizArchive;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.biz.AddBizToStaticDeployHook;
import com.alipay.sofa.ark.spi.service.extension.Extension;
import com.alipay.sofa.koupleless.arklet.core.command.CommandService;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletInitException;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletRuntimeException;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLogger;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.arklet.core.common.model.BaseMetadata;
import com.alipay.sofa.koupleless.arklet.core.hook.base.BaseMetadataHook;
import com.alipay.sofa.koupleless.arklet.tunnel.mqtt.executor.ExecutorServiceManager;
import com.alipay.sofa.koupleless.arklet.tunnel.mqtt.model.Constants;
import com.alipay.sofa.koupleless.arklet.tunnel.mqtt.model.MqttResponse;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.alipay.sofa.koupleless.arklet.core.command.builtin.BuiltinCommand.INSTALL_BIZ;

/**
 * @author 冬喃
 * @version : MqttMessageHandler, v 0.1 2024-09-12 下午3:01 your_name Exp $
 */
public class MqttMessageHandler {

    public static boolean          baselineQueryComplete = false;
    private final CommandService   commandService;
    private final BaseMetadataHook baseMetadataHook;
    private final MqttClient       mqttClient;
    private final UUID             deviceID;
    private String                 baseEnv;
    private final AtomicBoolean    run                   = new AtomicBoolean(false);

    public MqttMessageHandler(CommandService commandService, BaseMetadataHook baseMetadataHook,
                              MqttClient mqttClient, UUID deviceID, String baseEnv) {
        this.commandService = commandService;
        this.baseMetadataHook = baseMetadataHook;
        this.mqttClient = mqttClient;
        this.deviceID = deviceID;
        this.baseEnv = baseEnv;
        if (this.baseEnv == null || this.baseEnv.isEmpty()) {
            this.baseEnv = Constants.DEFAULT_BASE_ENV;
        } else {
            this.baseEnv = this.baseEnv.toLowerCase();
        }
    }

    /**
     * <p>getHealthTopic.</p>
     *
     * @return String
     */
    private String getHealthTopic() {
        return String.format("koupleless_%s/%s/base/health", baseEnv, deviceID);
    }

    /**
     * <p>getHeartBeatTopic.</p>
     *
     * @return String
     */
    private String getHeartBeatTopic() {
        return String.format("koupleless_%s/%s/base/heart", baseEnv, deviceID);
    }

    /**
     * <p>getBizTopic.</p>
     *
     * @return String
     */
    private String getBizTopic() {
        return String.format("koupleless_%s/%s/base/biz", baseEnv, deviceID);
    }

    /**
     * <p>getBizTopic.</p>
     *
     * @return String
     */
    private String getBizOperationResponseTopic() {
        return String.format("koupleless_%s/%s/base/bizOperation", baseEnv, deviceID);
    }

    /**
     * <p>getQueryBaselineTopic.</p>
     *
     * @return String
     */
    private String getQueryBaselineTopic() {
        return String.format("koupleless_%s/%s/base/queryBaseline", baseEnv, deviceID);
    }

    /**
     * <p>getBaselineTopic.</p>
     *
     * @return String
     */
    private String getBaselineTopic() {
        return String.format("koupleless_%s/%s/base/baseline", baseEnv, deviceID);
    }

    /**
     * <p>getCommandTopic.</p>
     *
     * @return String
     */
    private String getCommandTopic() {
        return String.format("koupleless_%s/%s/+", baseEnv, deviceID);
    }

    static class HeartBeatScheduledMission implements Runnable {

        private final String           topic;
        private final MqttClient       mqttClient;
        private final CommandService   commandService;
        private final BaseMetadataHook baseMetadataHook;

        public HeartBeatScheduledMission(String topic, MqttClient mqttClient,
                                         CommandService commandService,
                                         BaseMetadataHook baseMetadataHook) {
            this.topic = topic;
            this.mqttClient = mqttClient;
            this.commandService = commandService;
            this.baseMetadataHook = baseMetadataHook;
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

            try {
                InetAddress localHost = InetAddress.getLocalHost();
                networkInfo.put(Constants.LOCAL_IP, localHost.getHostAddress());
                networkInfo.put(Constants.LOCAL_HOST_NAME, localHost.getHostName());
            } catch (UnknownHostException e) {
                throw new ArkletRuntimeException("get local host failed", e);
            }

            heartBeatData.put(Constants.NETWORK_INFO, networkInfo);
            heartBeatData.put(Constants.State, BizState.ACTIVATED);

            try {
                mqttClient.publish(topic,
                    JSONObject.toJSONString(MqttResponse.withData(heartBeatData)).getBytes(), 1,
                    false);
            } catch (MqttException e) {
                throw new ArkletRuntimeException("mqtt client publish health status failed", e);
            }
        }
    }

    public void run() {
        try {
            mqttClient.subscribe(getCommandTopic(), 1);
            mqttClient.subscribe(getBaselineTopic(), 1);
        } catch (MqttException e) {
            throw new ArkletInitException("mqtt client subscribe command topic failed", e);
        }
        if (run.compareAndSet(false, true)) {
            // fetch baseline first
            BaseMetadata metadata = baseMetadataHook.getBaseMetadata();
            try {
                mqttClient.publish(getQueryBaselineTopic(),
                    JSONObject.toJSONString(MqttResponse.withData(metadata)).getBytes(), 1, false);
            } catch (MqttException e) {
                throw new ArkletInitException("publish query baseline msg error", e);
            }

            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

            executor.scheduleAtFixedRate(new HeartBeatScheduledMission(getHeartBeatTopic(),
                mqttClient, commandService, baseMetadataHook), 0, 120000L, TimeUnit.MILLISECONDS);
        }
    }

    public void handleCommand(String cmd, MqttMessage msg) throws ArkletRuntimeException {
        // process mqtt message, use thread pool to handle command
        ThreadPoolExecutor executor = ExecutorServiceManager.getArkTunnelMqttExecutor();
        executor.submit(() -> {
            Map<String, Object> cmdContent = JSONObject.parseObject(msg.toString(), HashMap.class);
            Output<?> output;
            try {
                // process the command
                output = commandService.process(cmd, cmdContent);
            } catch (InterruptedException e) {
                throw new ArkletRuntimeException(e);
            }

            try {
                if (cmd.equals(BuiltinCommand.HEALTH.getId())) {
                    // health command, send result to health topic
                    mqttClient.publish(getHealthTopic(),
                        JSONObject.toJSONString(MqttResponse.withData(output)).getBytes(), 1,
                        false);
                } else if (cmd.equals(BuiltinCommand.QUERY_ALL_BIZ.getId())) {
                    // queryAllBiz command, send result to biz topic
                    mqttClient.publish(getBizTopic(),
                        JSONObject.toJSONString(MqttResponse.withData(output)).getBytes(), 1,
                        false);
                } else {
                    // install or uninstall command, send result to biz operation response topic when failed
                    if (output.failed()) {
                        Map<String, Object> bizOperationResponse = new HashMap<>();
                        bizOperationResponse.put(Constants.COMMAND, cmd);
                        bizOperationResponse.put(Constants.COMMAND_RESPONSE, output);
                        mqttClient.publish(getBizOperationResponseTopic(), JSONObject
                            .toJSONString(MqttResponse.withData(bizOperationResponse)).getBytes(),
                            1, false);
                    } else {
                        // biz operation, need to sync biz status, queryAllBiz and send result to biz topic
                        Output<?> allBizOutput;
                        try {
                            allBizOutput = commandService
                                .process(BuiltinCommand.QUERY_ALL_BIZ.getId(), null);
                        } catch (InterruptedException e) {
                            throw new ArkletRuntimeException(e);
                        }
                        mqttClient.publish(getBizTopic(),
                            JSONObject.toJSONString(MqttResponse.withData(allBizOutput)).getBytes(),
                            1, false);
                    }
                }
            } catch (MqttException e) {
                throw new ArkletRuntimeException(e);
            }
        });
    }

}
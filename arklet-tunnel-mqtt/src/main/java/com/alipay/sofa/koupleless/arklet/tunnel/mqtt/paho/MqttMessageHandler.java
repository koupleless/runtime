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
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.koupleless.arklet.core.command.CommandService;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.model.BizInfo;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletInitException;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletRuntimeException;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.arklet.core.common.model.BaseMetadata;
import com.alipay.sofa.koupleless.arklet.core.hook.base.BaseMetadataHook;
import com.alipay.sofa.koupleless.arklet.core.hook.network.BaseNetworkInfoHook;
import com.alipay.sofa.koupleless.arklet.core.util.ExceptionUtils;
import com.alipay.sofa.koupleless.arklet.tunnel.mqtt.model.Constants;
import com.alipay.sofa.koupleless.arklet.tunnel.mqtt.model.MqttResponse;
import com.alipay.sofa.koupleless.arklet.tunnel.mqtt.model.SimpleBizInfo;
import com.alipay.sofa.koupleless.arklet.tunnel.mqtt.task.HeartBeatScheduledTask;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.alipay.sofa.koupleless.arklet.core.health.model.Constants.*;

/**
 * @author 冬喃
 * @version : MqttMessageHandler, v 0.1 2024-09-12 下午3:01 your_name Exp $
 */
public class MqttMessageHandler {

    public static boolean             baselineQueryComplete = false;
    private final CommandService      commandService;
    private final BaseMetadataHook    baseMetadataHook;
    private final BaseNetworkInfoHook baseNetworkInfoHook;
    private final MqttClient          mqttClient;
    private String                    baseEnv;
    private final AtomicBoolean       run                   = new AtomicBoolean(false);

    public MqttMessageHandler(CommandService commandService, BaseMetadataHook baseMetadataHook,
                              BaseNetworkInfoHook baseNetworkInfoHook, MqttClient mqttClient,
                              String baseEnv) {
        this.commandService = commandService;
        this.baseMetadataHook = baseMetadataHook;
        this.baseNetworkInfoHook = baseNetworkInfoHook;
        this.mqttClient = mqttClient;
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
        return String.format("koupleless_%s/%s/base/health", baseEnv, baseMetadataHook.getBaseID());
    }

    /**
     * <p>getHeartBeatTopic.</p>
     *
     * @return String
     */
    private String getHeartBeatTopic() {
        return String.format("koupleless_%s/%s/base/heart", baseEnv, baseMetadataHook.getBaseID());
    }

    /**
     * <p>getBizTopic.</p>
     *
     * @return String
     */
    private String getBizTopic() {
        return String.format("koupleless_%s/%s/base/simpleBiz", baseEnv,
            baseMetadataHook.getBaseID());
    }

    /**
     * <p>getBizTopic.</p>
     *
     * @return String
     */
    private String getBizOperationResponseTopic() {
        return String.format("koupleless_%s/%s/base/bizOperation", baseEnv,
            baseMetadataHook.getBaseID());
    }

    /**
     * <p>getQueryBaselineTopic.</p>
     *
     * @return String
     */
    private String getQueryBaselineTopic() {
        return String.format("koupleless_%s/%s/base/queryBaseline", baseEnv,
            baseMetadataHook.getBaseID());
    }

    /**
     * <p>getBaselineTopic.</p>
     *
     * @return String
     */
    private String getBaselineTopic() {
        return String.format("koupleless_%s/%s/base/baseline", baseEnv,
            baseMetadataHook.getBaseID());
    }

    /**
     * <p>getCommandTopic.</p>
     *
     * @return String
     */
    private String getCommandTopic() {
        return String.format("koupleless_%s/%s/+", baseEnv, baseMetadataHook.getBaseID());
    }

    public void onConnectCompleted() {
        try {
            mqttClient.subscribe(getCommandTopic(), 1);
            mqttClient.subscribe(getBaselineTopic(), 1);
            ArkletLoggerFactory.getDefaultLogger().info("mqtt client subscribe command topic: {}",
                getCommandTopic());
            ArkletLoggerFactory.getDefaultLogger().info("mqtt client subscribe command topic: {}",
                getBaselineTopic());
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

            executor.scheduleAtFixedRate(new HeartBeatScheduledTask(getHeartBeatTopic(), mqttClient,
                baseMetadataHook, baseNetworkInfoHook), 0, 10000L, TimeUnit.MILLISECONDS);
        }
    }

    private List<SimpleBizInfo> getSimpleAllBizInfo(Output<?> output) {
        Output<List<BizInfo>> queryAllBizOutput = (Output<List<BizInfo>>) output;
        List<SimpleBizInfo> simpleData = new ArrayList<>();
        for (BizInfo info : queryAllBizOutput.getData()) {
            simpleData.add(SimpleBizInfo.constructFromBizInfo(info));
        }
        return simpleData;
    }

    public void handleCommand(String cmd, MqttMessage msg) throws ArkletRuntimeException {
        ArkletLoggerFactory.getDefaultLogger().info("mqtt handle command {} with content {}", cmd,
            msg);
        Output<?> output;
        try {
            if (cmd.equals(BuiltinCommand.INSTALL_BIZ.getId())
                || cmd.equals(BuiltinCommand.UNINSTALL_BIZ.getId())) {
                Map<String, Object> cmdContent = JSONObject.parseObject(msg.toString(),
                    HashMap.class);
                output = commandService.process(cmd, cmdContent);
                // install or uninstall command, send result to biz operation response topic
                Map<String, Object> bizOperationResponse = new HashMap<>();
                bizOperationResponse.put(Constants.COMMAND, cmd);
                bizOperationResponse.put(BIZ_NAME, cmdContent.get(BIZ_NAME));
                bizOperationResponse.put(BIZ_VERSION, cmdContent.get(BIZ_VERSION));
                bizOperationResponse.put(Constants.COMMAND_RESPONSE, output);
                mqttClient.publish(getBizOperationResponseTopic(),
                    JSONObject.toJSONString(MqttResponse.withData(bizOperationResponse)).getBytes(),
                    1, false);
                // sync biz status after operation, queryAllBiz and send result to biz topic
                Output<?> allBizOutput = commandService
                    .process(BuiltinCommand.QUERY_ALL_BIZ.getId(), null);
                mqttClient.publish(getBizTopic(), JSONObject
                    .toJSONString(MqttResponse.withData(getSimpleAllBizInfo(allBizOutput)),
                        SerializerFeature.SkipTransientField, SerializerFeature.WriteMapNullValue)
                    .getBytes(), 1, false);
            } else if (cmd.equals("baseline")) {
                List<Map<String, Object>> cmdContents = JSONObject.parseObject(msg.toString(),
                    List.class);
                // TODO: parallel process
                for (Map<String, Object> cmdContent : cmdContents) {
                    commandService.process(BuiltinCommand.INSTALL_BIZ.getId(), cmdContent);
                }
                Output<?> allBizOutput = commandService
                    .process(BuiltinCommand.QUERY_ALL_BIZ.getId(), null);
                mqttClient.publish(getBizTopic(), JSONObject
                    .toJSONString(MqttResponse.withData(getSimpleAllBizInfo(allBizOutput)),
                        SerializerFeature.SkipTransientField, SerializerFeature.WriteMapNullValue)
                    .getBytes(), 1, false);
            } else if (cmd.equals(BuiltinCommand.HEALTH.getId())) {
                // health command, send result to health topic
                Map<String, Object> cmdContent = JSONObject.parseObject(msg.toString(),
                    HashMap.class);
                output = commandService.process(cmd, cmdContent);
                mqttClient.publish(getHealthTopic(),
                    JSONObject.toJSONString(MqttResponse.withData(output)).getBytes(), 1, false);
            } else if (cmd.equals(BuiltinCommand.QUERY_ALL_BIZ.getId())) {
                // queryAllBiz command, send result to biz topic
                Map<String, Object> cmdContent = JSONObject.parseObject(msg.toString(),
                    HashMap.class);
                output = commandService.process(cmd, cmdContent);
                mqttClient.publish(getBizTopic(),
                    JSONObject.toJSONString(MqttResponse.withData(getSimpleAllBizInfo(output)),
                        SerializerFeature.SkipTransientField, SerializerFeature.WriteMapNullValue)
                        .getBytes(),
                    1, false);
            } else {
                throw new ArkletRuntimeException(
                    String.format("unsupported command %s or content %s", cmd, msg));
            }
        } catch (Throwable e) {
            ClientResponse data = new ClientResponse();
            data.setMessage(ExceptionUtils.getStackTraceAsString(e));
            data.setCode(ResponseCode.FAILED);
            output = Output.ofFailed(data, e.getMessage());

            // install or uninstall command, send result to biz operation response topic
            Map<String, Object> bizOperationResponse = new HashMap<>();
            bizOperationResponse.put(Constants.COMMAND, cmd);
            bizOperationResponse.put(Constants.COMMAND_RESPONSE, output);
            try {
                mqttClient.publish(getBizOperationResponseTopic(),
                    JSONObject.toJSONString(MqttResponse.withData(bizOperationResponse)).getBytes(),
                    1, false);
            } catch (MqttException ex) {
                throw new ArkletRuntimeException(ex);
            }
            throw new ArkletRuntimeException(e);
        }
    }

}

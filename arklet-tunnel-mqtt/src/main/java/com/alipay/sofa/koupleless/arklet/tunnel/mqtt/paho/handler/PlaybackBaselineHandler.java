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
package com.alipay.sofa.koupleless.arklet.tunnel.mqtt.paho.handler;

import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.koupleless.arklet.core.command.CommandService;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletRuntimeException;
import com.alipay.sofa.koupleless.arklet.core.hook.base.BaseMetadataHook;
import com.alipay.sofa.koupleless.arklet.core.util.ExceptionUtils;
import com.alipay.sofa.koupleless.arklet.tunnel.mqtt.model.Constants;
import com.alipay.sofa.koupleless.arklet.tunnel.mqtt.model.MqttResponse;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: PlaybackBaselineHandler.java, v 0.1 2024年12月30日 14:21 立蓬 Exp $
 */
public class PlaybackBaselineHandler extends MsgHandler {
    public static final String     Mqtt_CMD = "baseline";

    private final MqttClient       mqttClient;

    private final BaseMetadataHook baseMetadataHook;

    private final String           baseEnv;

    public PlaybackBaselineHandler(CommandService commandService, BaseMetadataHook baseMetadataHook,
                                   MqttClient mqttClient, String baseEnv) {
        super(commandService);
        this.mqttClient = mqttClient;
        this.baseMetadataHook = baseMetadataHook;
        this.baseEnv = baseEnv;
    }

    @Override
    protected String getMqttCMD() {
        return Mqtt_CMD;
    }

    @Override
    protected BuiltinCommand getArkletCommand() {
        return BuiltinCommand.BATCH_INSTALL_BIZ;
    }

    @Override
    protected void pubThrowableMsg(Throwable e) {
        Map<String, Object> bizOperationResponse = failOfResponse(e);
        try {
            mqttClient.publish(getPubTopic(),
                JSONObject.toJSONString(MqttResponse.withData(bizOperationResponse)).getBytes(), 1,
                false);
        } catch (MqttException ex) {
            throw new ArkletRuntimeException(ex);
        }
    }

    @Override
    protected void pubSuccessMsg(Output<?> output) throws MqttException {
        Map<String, Object> bizOperationResponse = successOfResponse(output);
        mqttClient.publish(getPubTopic(),
            JSONObject.toJSONString(MqttResponse.withData(bizOperationResponse)).getBytes(), 1,
            false);
    }

    private String getPubTopic() {
        return String.format("koupleless_%s/%s/base/batchInstallBizResponse", baseEnv,
            baseMetadataHook.getIdentity());
    }

    @Override
    public Map<String, Object> toRequest(MqttMessage msg) {
        List<Map<String, Object>> cmdContents = JSONObject.parseObject(msg.toString(), List.class);
        Map<String, Object> cmdContent = new HashMap<>();
        cmdContent.put("bizList", cmdContents);
        return cmdContent;
    }

    /**
     * success mqtt response like:
     * { "command" -> "batchInstallBiz" }
     * { "response" -> output }
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    private Map<String, Object> successOfResponse(Output<?> output) {
        Map<String, Object> bizOperationResponse = new HashMap<>();
        bizOperationResponse.put(Constants.COMMAND, getArkletCommand().getId());
        bizOperationResponse.put(Constants.COMMAND_RESPONSE, output);
        return bizOperationResponse;
    }

    /**
     * failed mqtt response like:
     * { "command" -> "batchInstallBiz" }
     * { "response" -> output }
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    private Map<String, Object> failOfResponse(Throwable e) {
        ClientResponse data = new ClientResponse();
        data.setMessage(ExceptionUtils.getStackTraceAsString(e));
        data.setCode(ResponseCode.FAILED);
        Output<?> output = Output.ofFailed(data, e.getMessage());

        Map<String, Object> bizOperationResponse = new HashMap<>();
        bizOperationResponse.put(Constants.COMMAND, getArkletCommand().getId());
        bizOperationResponse.put(Constants.COMMAND_RESPONSE, output);
        return bizOperationResponse;
    }
}
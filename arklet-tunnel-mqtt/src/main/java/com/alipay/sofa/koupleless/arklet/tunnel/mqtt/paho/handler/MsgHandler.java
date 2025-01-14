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

import com.alipay.sofa.koupleless.arklet.core.command.CommandService;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletRuntimeException;
import com.alipay.sofa.koupleless.common.log.ArkletLoggerFactory;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Map;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: MsgHandler.java, v 0.1 2024年12月30日 14:22 立蓬 Exp $
 */
public abstract class MsgHandler {

    protected final CommandService commandService;

    public MsgHandler(CommandService commandService) {
        this.commandService = commandService;
    }

    public void handle(MqttMessage msg) {
        Map<String, Object> cmdContent = toRequest(msg);
        ArkletLoggerFactory.getDefaultLogger().info("start to handle mqtt cmd {} with content: {}",
            getMqttCMD(), cmdContent);

        Output<?> output = null;
        try {
            output = commandService.process(getArkletCommand().getId(), cmdContent);
            pubSuccessMsg(output);
        } catch (Throwable e) {
            ArkletLoggerFactory.getDefaultLogger().error(String.format(
                "fail to handle mqtt cmd %s with content: %s", getMqttCMD(), e.getMessage()));
            pubThrowableMsg(e);
            throw new ArkletRuntimeException(e);
        }

        ArkletLoggerFactory.getDefaultLogger()
            .info("finished to handle mqtt cmd {} when with output: {}", getMqttCMD(), output);
    }

    abstract Map<String, Object> toRequest(MqttMessage msg);

    abstract String getMqttCMD();

    abstract BuiltinCommand getArkletCommand();

    abstract void pubSuccessMsg(Output<?> output) throws Exception;

    abstract void pubThrowableMsg(Throwable e);
}
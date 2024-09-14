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
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.koupleless.arklet.core.common.model.BaseMetadata;
import com.alipay.sofa.koupleless.arklet.core.hook.base.BaseMetadataHook;
import com.alipay.sofa.koupleless.arklet.tunnel.mqtt.executor.ExecutorServiceManager;
import com.alipay.sofa.koupleless.arklet.tunnel.mqtt.model.MqttResponse;
import com.alipay.sofa.koupleless.arklet.core.command.CommandService;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletInitException;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletRuntimeException;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLogger;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.arklet.tunnel.mqtt.model.Constants;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.eclipse.paho.client.mqttv3.MqttException.REASON_CODE_SOCKET_FACTORY_MISMATCH;

/**
 * <p>NettyHttpServer class.</p>
 *
 * @author dongnan
 * @since 2024/07/05
 * @version 1.0.0
 */

@SuppressWarnings("unchecked")
public class PahoMqttClient {

    private final MqttClient          mqttClient;
    private final UUID                deviceID;
    private final String              env;
    private final MqttConnectOptions  options = new MqttConnectOptions();

    private final CommandService      commandService;
    private static final ArkletLogger LOGGER  = ArkletLoggerFactory.getDefaultLogger();
    private final BaseMetadataHook    baseMetadataHook;

    /**
     * <p>Constructor for PahoMqttClient.</p>
     *
     * @param broker String
     * @param port int
     * @param username String
     * @param password char[]
     * @param commandService a {@link com.alipay.sofa.koupleless.arklet.core.command.CommandService} object
     * @param deviceID a {@link java.util.UUID} object
     * @param clientPrefix a {@link java.lang.String} object
     * @throws org.eclipse.paho.client.mqttv3.MqttException if any.
     */
    public PahoMqttClient(String broker, int port, UUID deviceID, String clientPrefix,
                          String username, String password, CommandService commandService,
                          BaseMetadataHook baseMetadataHook) throws MqttException {
        this.deviceID = deviceID;
        this.mqttClient = new MqttClient(String.format("tcp://%s:%d", broker, port),
            String.format("%s@@@%s", clientPrefix, deviceID), new MemoryPersistence());
        this.options.setAutomaticReconnect(true);
        this.options.setMaxInflight(1000);
        this.options.setUserName(username);
        this.options.setPassword(password.toCharArray());

        this.baseMetadataHook = baseMetadataHook;
        this.env = this.baseMetadataHook.getRuntimeEnv();

        this.commandService = commandService;
    }

    /**
     * <p>Constructor for PahoMqttClient.</p>
     *
     * @param broker String
     * @param port int
     * @param caFilePath String
     * @param clientCrtFilePath String
     * @param clientKeyFilePath String
     * @param commandService a {@link com.alipay.sofa.koupleless.arklet.core.command.CommandService} object
     * @param deviceID a {@link java.util.UUID} object
     * @param clientPrefix a {@link java.lang.String} object
     * @param username a {@link java.lang.String} object
     * @param password a {@link java.lang.String} object
     * @throws org.eclipse.paho.client.mqttv3.MqttException if any.
     */
    public PahoMqttClient(String broker, int port, UUID deviceID, String clientPrefix,
                          String username, String password, String caFilePath,
                          String clientCrtFilePath, String clientKeyFilePath,
                          CommandService commandService,
                          BaseMetadataHook baseMetadataHook) throws MqttException {
        this.deviceID = deviceID;
        this.mqttClient = new MqttClient(String.format("ssl://%s:%d", broker, port),
            String.format("%s@@@%s", clientPrefix, deviceID), new MemoryPersistence());
        this.options.setCleanSession(true);
        this.options.setAutomaticReconnect(true);
        this.options.setMaxInflight(1000);
        this.options.setUserName(username);
        this.options.setPassword(password.toCharArray());
        this.baseMetadataHook = baseMetadataHook;
        this.env = this.baseMetadataHook.getRuntimeEnv();
        try {
            this.options.setSocketFactory(
                SSLUtils.getSocketFactory(caFilePath, clientCrtFilePath, clientKeyFilePath, ""));
        } catch (Exception e) {
            throw new MqttException(REASON_CODE_SOCKET_FACTORY_MISMATCH, e);
        }

        this.commandService = commandService;
    }

    /**
     * <p>open.</p>
     *
     * @throws org.eclipse.paho.client.mqttv3.MqttException if any.
     */
    public void open() throws MqttException {
        this.mqttClient.setCallback(new PahoMqttCallback(this.mqttClient, this.commandService,
            this.baseMetadataHook, this.deviceID, this.env));
        this.mqttClient.connect(this.options);
    }

    /**
     * <p>close.</p>
     *
     * @throws org.eclipse.paho.client.mqttv3.MqttException is any
     */
    public void close() throws MqttException {
        this.mqttClient.disconnect();
        this.mqttClient.close();
    }

    static class PahoMqttCallback implements MqttCallbackExtended {

        private final MqttMessageHandler messageHandler;

        public PahoMqttCallback(MqttClient mqttClient, CommandService commandService,
                                BaseMetadataHook baseMetadataHook, UUID deviceID, String env) {
            this.messageHandler = new MqttMessageHandler(commandService, baseMetadataHook,
                mqttClient, deviceID, env);
        }

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            // connect success callback, start subscriptions watch here
            if (reconnect) {
                LOGGER.info("mqtt client reconnect successfully");
            } else {
                LOGGER.info("mqtt client connect successfully");
            }
            messageHandler.run();
        }

        @Override
        public void connectionLost(Throwable throwable) {
            // disconnect callback, print error log to locate it
            LOGGER.error("mqtt client connection lost", throwable);
        }

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
            // process mqtt message, use thread pool to handle command
            ThreadPoolExecutor executor = ExecutorServiceManager.getArkTunnelMqttExecutor();
            executor.submit(() -> {
                // message callback, don't pub sync message here, may cause deadlock
                String[] topicSplits = topic.split("/");
                messageHandler.handleCommand(topicSplits[topicSplits.length - 1], mqttMessage);
            });
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            // send message successfully, no former process here
        }

    }
}

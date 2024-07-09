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
package com.alipay.sofa.koupleless.arklet.core.api.tunnel.mqtt.paho;

import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.koupleless.arklet.core.command.CommandService;
import com.alipay.sofa.koupleless.arklet.core.command.executor.ExecutorServiceManager;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletInitException;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLogger;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.arklet.core.health.model.Health;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

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
    private UUID                      deviceID;
    private final MqttConnectOptions  options = new MqttConnectOptions();

    private final CommandService      commandService;
    private static final ArkletLogger LOGGER  = ArkletLoggerFactory.getDefaultLogger();

    /**
     * <p>Constructor for PahoMqttClient.</p>
     *
     * @param broker String
     * @param port int
     * @param username String
     * @param password char[]
     * @param commandService a {@link CommandService} object
     */
    public PahoMqttClient(String broker, int port, UUID deviceID, String username, String password,
                          CommandService commandService) throws MqttException {
        this.deviceID = deviceID;
        this.mqttClient = new MqttClient(String.format("tcp://%s:%d", broker, port),
            String.format("%s@@@%s", "koupleless", deviceID), new MemoryPersistence());
        this.options.setKeepAliveInterval(60);
        this.options.setAutomaticReconnect(true);
        this.options.setMaxInflight(1000);
        this.options.setUserName(username);
        this.options.setPassword(password.toCharArray());

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
     * @param commandService a {@link CommandService} object
     */
    public PahoMqttClient(String broker, int port, UUID deviceID, String caFilePath,
                          String clientCrtFilePath, String clientKeyFilePath,
                          CommandService commandService) throws MqttException {
        this.deviceID = deviceID;
        this.mqttClient = new MqttClient(String.format("ssl://%s:%d", broker, port),
            String.format("%s@@@%s", "koupleless", deviceID), new MemoryPersistence());
        this.options.setCleanSession(true);
        this.options.setKeepAliveInterval(60);
        this.options.setAutomaticReconnect(true);
        this.options.setMaxInflight(1000);
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
     * @throws MqttException if any.
     */
    public void open() throws MqttException {
        this.mqttClient
            .setCallback(new PahoMqttCallback(this.mqttClient, this.commandService, this.deviceID));
        this.mqttClient.connect(this.options);
    }

    /**
     * <p>close.</p>
     *
     * @throws MqttException is any
     */
    public void close() throws MqttException {
        this.mqttClient.disconnect();
        this.mqttClient.close();
    }

    static class PahoMqttCallback implements MqttCallbackExtended {

        private MqttMessageHandler messageHandler;

        public PahoMqttCallback(MqttClient mqttClient, CommandService commandService,
                                UUID deviceID) {
            this.messageHandler = new MqttMessageHandler(commandService, mqttClient, deviceID);
        }

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            messageHandler.onStart();
            // connect success callback, start subscriptions watch here
            if (reconnect) {
                LOGGER.info("mqtt client reconnect successfully");
                return;
            }
            LOGGER.info("mqtt client connect successfully");

        }

        @Override
        public void connectionLost(Throwable throwable) {
            // disconnect callback, print error log to locate it
            LOGGER.error("mqtt client connection lost", throwable);
        }

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
            // message callback, don't pub sync message here, may cause deadlock
            String[] topicSplits = topic.split("/");
            messageHandler.handle(topicSplits[topicSplits.length - 1], mqttMessage);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            // 发送成功
        }

    }

    public static class MqttMessageHandler {
        private CommandService commandService;
        private MqttClient     mqttClient;
        private UUID           deviceID;

        public MqttMessageHandler(CommandService commandService, MqttClient mqttClient,
                                  UUID deviceID) {
            this.commandService = commandService;
            this.mqttClient = mqttClient;
            this.deviceID = deviceID;
        }

        /**
         * <p>getHealthTopic.</p>
         *
         * @return String
         */
        private String getHealthTopic() {
            return String.format("koupleless/%s/base/health", deviceID);
        }

        /**
         * <p>getHeartBeatTopic.</p>
         *
         * @return String
         */
        private String getHeartBeatTopic() {
            return String.format("koupleless/%s/base/heart", deviceID);
        }

        /**
         * <p>getBizTopic.</p>
         *
         * @return String
         */
        private String getBizTopic() {
            return String.format("koupleless/%s/base/biz", deviceID);
        }

        /**
         * <p>getCommandTopic.</p>
         *
         * @return String
         */
        private String getCommandTopic() {
            return String.format("koupleless/%s/+", deviceID);
        }

        static class HeartBeatScheduledMission implements Runnable {

            private String         topic;
            private MqttClient     mqttClient;
            private CommandService commandService;

            public HeartBeatScheduledMission(String topic, MqttClient mqttClient,
                                             CommandService commandService) {
                this.topic = topic;
                this.mqttClient = mqttClient;
                this.commandService = commandService;
            }

            @Override
            public void run() {
                Object healthData;

                try {
                    Output<?> output = commandService.process("health", new HashMap());
                    Health data = (Health) output.getData();
                    healthData = data.getHealthData().get("masterBizInfo");
                } catch (InterruptedException e) {
                    LOGGER.info("get health status failed");
                    throw new ArkletInitException("get health status failed", e);
                }

                try {
                    mqttClient.publish(topic, JSONObject.toJSONString(healthData).getBytes(), 0,
                        false);
                } catch (MqttException e) {
                    LOGGER.info("mqtt client publish health status failed");
                    throw new ArkletInitException("mqtt client publish health status failed", e);
                }
            }
        }

        public void onStart() {
            try {
                mqttClient.subscribe(getCommandTopic(), 1);
            } catch (MqttException e) {
                LOGGER.info("mqtt client subscribe command topic failed");
                throw new ArkletInitException("mqtt client subscribe command topic failed", e);
            }

            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

            executor.scheduleAtFixedRate(
                new HeartBeatScheduledMission(getHeartBeatTopic(), mqttClient, commandService), 0,
                2000L, TimeUnit.MILLISECONDS);
        }

        public void handle(String cmd, MqttMessage msg) {
            LOGGER.info(String.format("handle message: %s, cmd: %s", msg.toString(), cmd));
            ThreadPoolExecutor executor = ExecutorServiceManager.getArkMqttOpsExecutor();
            executor.submit(() -> {
                RequestValidation validation = RequestValidation.passed(true, cmd,
                    JSONObject.parseObject(msg.toString(), HashMap.class));
                Output<?> output;
                if (cmd.equals("installBiz")) {
                    LOGGER.info(validation.getCmd(),
                        JSONObject.toJSONString(validation.getCmdContent()));
                }
                try {
                    output = commandService.process(validation.getCmd(),
                        validation.getCmdContent());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    if (cmd.equals("health")) {
                        mqttClient.publish(getHealthTopic(),
                            JSONObject.toJSONString(output).getBytes(), 1, false);
                    } else if (cmd.equals("queryAllBiz")) {
                        mqttClient.publish(getBizTopic(),
                            JSONObject.toJSONString(output).getBytes(), 1, false);
                    } else {
                        // biz operation, need to sync biz status
                        Output<?> allBizOutput;
                        try {
                            allBizOutput = commandService.process("queryAllBiz", new HashMap());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        mqttClient.publish(getBizTopic(),
                            JSONObject.toJSONString(allBizOutput).getBytes(), 1, false);
                    }
                } catch (MqttException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}

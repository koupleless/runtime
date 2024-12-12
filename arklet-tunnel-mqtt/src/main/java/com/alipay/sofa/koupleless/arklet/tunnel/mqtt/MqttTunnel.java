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
package com.alipay.sofa.koupleless.arklet.tunnel.mqtt;

import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.koupleless.arklet.core.api.tunnel.Tunnel;
import com.alipay.sofa.koupleless.arklet.core.hook.base.BaseMetadataHook;
import com.alipay.sofa.koupleless.arklet.tunnel.mqtt.paho.PahoMqttClient;
import com.alipay.sofa.koupleless.arklet.core.command.CommandService;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletInitException;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletRuntimeException;
import com.alipay.sofa.koupleless.common.log.ArkletLogger;
import com.alipay.sofa.koupleless.common.log.ArkletLoggerFactory;
import com.google.inject.Singleton;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>MqttTunnel class.</p>
 *
 * @author dongnan
 * @since 2024/7/5
 * @version 1.0.0
 */

@Singleton
public class MqttTunnel implements Tunnel {

    private static final ArkletLogger                                     LOGGER                              = ArkletLoggerFactory
        .getDefaultLogger();
    private final static String                                           MQTT_ENABLE_ATTRIBUTE               = "koupleless.arklet.mqtt.enable";
    private final static String                                           MQTT_BROKER_ATTRIBUTE               = "koupleless.arklet.mqtt.broker";
    private final static String                                           MQTT_PORT_ATTRIBUTE                 = "koupleless.arklet.mqtt.port";
    private final static String                                           MQTT_USERNAME_ATTRIBUTE             = "koupleless.arklet.mqtt.username";
    private final static String                                           MQTT_PASSWORD_ATTRIBUTE             = "koupleless.arklet.mqtt.password";
    private final static String                                           MQTT_CA_FILE_PATH_ATTRIBUTE         = "koupleless.arklet.mqtt.ca_path";
    private final static String                                           MQTT_CLIENT_CRT_FILE_PATH_ATTRIBUTE = "koupleless.arklet.mqtt.client_crt_path";
    private final static String                                           MQTT_CLIENT_KEY_FILE_PATH_ATTRIBUTE = "koupleless.arklet.mqtt.client_key_path";
    private final static String                                           MQTT_CLIENT_PREFIX_ATTRIBUTE        = "koupleless.arklet.mqtt.client.prefix";
    private PahoMqttClient                                                pahoMqttClient;
    private final AtomicBoolean                                           shutdown                            = new AtomicBoolean(
        false);
    private final AtomicBoolean                                           init                                = new AtomicBoolean(
        false);
    private final AtomicBoolean                                           run                                 = new AtomicBoolean(
        false);

    private com.alipay.sofa.koupleless.arklet.core.command.CommandService commandService;
    private BaseMetadataHook                                              baseMetadataHook;
    private boolean                                                       enable                              = false;
    private int                                                           port;
    private String                                                        brokerUrl;
    private String                                                        username;
    private String                                                        password;
    private String                                                        clientPrefix;
    private String                                                        caFilePath;
    private String                                                        clientCrtFilePath;
    private String                                                        clientKeyFilePath;

    /** {@inheritDoc} */
    @Override
    public void init(CommandService commandService, BaseMetadataHook baseMetadataHook) {
        if (init.compareAndSet(false, true)) {
            String enable = EnvironmentUtils.getProperty(MQTT_ENABLE_ATTRIBUTE);
            if (enable == null || !enable.equals("true")) {
                // mqtt not enable, return
                return;
            }
            this.enable = true;
            this.commandService = commandService;
            this.baseMetadataHook = baseMetadataHook;

            String brokerPort = EnvironmentUtils.getProperty(MQTT_PORT_ATTRIBUTE);
            this.brokerUrl = EnvironmentUtils.getProperty(MQTT_BROKER_ATTRIBUTE);
            this.username = EnvironmentUtils.getProperty(MQTT_USERNAME_ATTRIBUTE);
            this.password = EnvironmentUtils.getProperty(MQTT_PASSWORD_ATTRIBUTE);
            this.clientPrefix = EnvironmentUtils.getProperty(MQTT_CLIENT_PREFIX_ATTRIBUTE);
            this.caFilePath = EnvironmentUtils.getProperty(MQTT_CA_FILE_PATH_ATTRIBUTE);
            this.clientCrtFilePath = EnvironmentUtils
                .getProperty(MQTT_CLIENT_CRT_FILE_PATH_ATTRIBUTE);
            this.clientKeyFilePath = EnvironmentUtils
                .getProperty(MQTT_CLIENT_KEY_FILE_PATH_ATTRIBUTE);

            if (StringUtils.isEmpty(brokerPort)) {
                LOGGER.error("Invalid arklet mqtt port: empty");
                throw new com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletInitException(
                    "Invalid arklet mqtt port: empty");
            }

            if (StringUtils.isEmpty(this.username)) {
                LOGGER.error("Invalid arklet mqtt username: empty");
                throw new com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletInitException(
                    "Invalid arklet mqtt username: empty");
            }

            if (StringUtils.isEmpty(this.password)) {
                LOGGER.error("Invalid arklet mqtt password: empty");
                throw new com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletInitException(
                    "Invalid arklet mqtt password: empty");
            }
            if (StringUtils.isEmpty(this.clientPrefix)) {
                LOGGER.error("Invalid arklet mqtt clientPrefix: empty");
                throw new com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletInitException(
                    "Invalid arklet mqtt clientPrefix: empty");
            }

            try {
                this.port = Integer.parseInt(brokerPort);
            } catch (NumberFormatException e) {
                LOGGER.error(String.format("Invalid arklet http port in %s", brokerPort), e);
                throw new com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletInitException(
                    e);
            }

            if (StringUtils.isEmpty(this.brokerUrl)) {
                LOGGER.error("Invalid arklet mqtt broker url: empty");
                throw new ArkletInitException("Invalid arklet mqtt broker url: empty");
            }

            LOGGER.info("mqtt tunnel initialized: {}", this);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        if (run.compareAndSet(false, true)) {
            if (!this.enable) {
                return;
            }
            AssertUtils.isTrue(this.port > 0, "mqtt port should be positive integer.");
            AssertUtils.isTrue(
                !StringUtils.isEmpty(this.username) || !StringUtils.isEmpty(this.caFilePath),
                "at least one identity should be provided.");

            try {
                LOGGER.info("mqtt tunnel starting");
                if (!StringUtils.isEmpty(this.caFilePath)) {
                    // init mqtt client with ca and client crt
                    pahoMqttClient = new PahoMqttClient(brokerUrl, port, clientPrefix, username,
                        password, caFilePath, clientCrtFilePath, clientKeyFilePath, commandService,
                        baseMetadataHook);
                } else {
                    // init mqtt client with username and password
                    pahoMqttClient = new PahoMqttClient(brokerUrl, port, clientPrefix, username,
                        password, commandService, baseMetadataHook);
                }
                pahoMqttClient.open();
            } catch (MqttException e) {
                LOGGER.error("Unable to open mqtt client.", e);
                throw new ArkletRuntimeException(e);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            try {
                if (pahoMqttClient != null) {
                    pahoMqttClient.close();
                    pahoMqttClient = null;
                }
            } catch (Throwable t) {
                LOGGER.error("An error occurs when shutdown arklet mqtt client.", t);
                throw new ArkletRuntimeException(t);
            }
        }
    }

}

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
package com.alipay.sofa.koupleless.arklet.core.api.tunnel.http;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.ark.common.util.PortSelectUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.koupleless.arklet.core.api.tunnel.Tunnel;
import com.alipay.sofa.koupleless.arklet.core.api.tunnel.http.netty.NettyHttpServer;
import com.alipay.sofa.koupleless.arklet.core.command.CommandService;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletInitException;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletRuntimeException;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLogger;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.arklet.core.common.model.BaseMetadata;
import com.alipay.sofa.koupleless.arklet.core.hook.base.BaseMetadataHook;
import com.google.inject.Singleton;

import static com.alipay.sofa.koupleless.arklet.core.health.model.Constants.*;

/**
 * <p>HttpTunnel class.</p>
 *
 * @author mingmen
 * @since 2023/6/8
 * @version 1.0.0
 */

@Singleton
public class HttpTunnel implements Tunnel {

    private static final ArkletLogger LOGGER                   = ArkletLoggerFactory
        .getDefaultLogger();
    private final static String       HTTP_PORT_ATTRIBUTE      = "koupleless.arklet.http.port";
    // http tunnel heart beat endpoint
    private final static String       HEART_BEAT_ENDPOINT      = "koupleless.arklet.http.heartbeat.endpoint";
    private int                       port                     = -1;
    private final static int          DEFAULT_HTTP_PORT        = 1238;
    private final static int          DEFAULT_SELECT_PORT_SIZE = 100;
    private NettyHttpServer           nettyHttpServer;
    private final AtomicBoolean       shutdown                 = new AtomicBoolean(false);
    private final AtomicBoolean       init                     = new AtomicBoolean(false);
    private final AtomicBoolean       run                      = new AtomicBoolean(false);
    private CommandService            commandService;
    private BaseMetadataHook          baseMetadataHook;
    private UUID                      baseID;

    /** {@inheritDoc} */
    @Override
    public void init(CommandService commandService, BaseMetadataHook baseMetadataHook,
                     UUID baseID) {
        if (init.compareAndSet(false, true)) {
            this.commandService = commandService;
            this.baseMetadataHook = baseMetadataHook;
            this.baseID = baseID;
            String httpPort = EnvironmentUtils.getProperty(HTTP_PORT_ATTRIBUTE);
            try {
                if (!StringUtils.isEmpty(httpPort)) {
                    port = Integer.parseInt(httpPort);
                } else {
                    port = PortSelectUtils.selectAvailablePort(DEFAULT_HTTP_PORT,
                        DEFAULT_SELECT_PORT_SIZE);
                }
            } catch (NumberFormatException e) {
                LOGGER.error(String.format("Invalid arklet http port in %s", httpPort), e);
                throw new ArkletInitException(e);
            }
            // read endpoint by EnvironmentUtils, if not empty, start a thread to post metadata every 2 minutes
            String heartBeatEndpoint = EnvironmentUtils.getProperty(HEART_BEAT_ENDPOINT);
            if (!StringUtils.isEmpty(heartBeatEndpoint)) {
                ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

                executor.scheduleAtFixedRate(new HeartBeatScheduledMission(baseID, port,
                    heartBeatEndpoint, commandService, baseMetadataHook), 0, 120000L,
                    TimeUnit.MILLISECONDS);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        if (run.compareAndSet(false, true)) {
            AssertUtils.isTrue(port > 0, "Http port should be positive integer.");
            try {
                LOGGER.info("http tunnel listening on port: " + port);
                nettyHttpServer = new NettyHttpServer(port, commandService);
                nettyHttpServer.open(baseID);
            } catch (InterruptedException e) {
                LOGGER.error("Unable to open netty schedule http server.", e);
                throw new ArkletRuntimeException(e);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            try {
                if (nettyHttpServer != null) {
                    nettyHttpServer.close();
                    nettyHttpServer = null;
                }
            } catch (Throwable t) {
                LOGGER.error("An error occurs when shutdown arklet http server.", t);
                throw new ArkletRuntimeException(t);
            }
        }
    }

    static class HeartBeatScheduledMission implements Runnable {

        private final String           heartBeatEndpoint;
        private UUID                   baseID;
        private int                    port;
        private final CommandService   commandService;
        private final BaseMetadataHook baseMetadataHook;

        public HeartBeatScheduledMission(UUID baseID, int port, String heartBeatEndpoint,
                                         CommandService commandService,
                                         BaseMetadataHook baseMetadataHook) {
            this.baseID = baseID;
            this.port = port;
            this.heartBeatEndpoint = heartBeatEndpoint;
            this.commandService = commandService;
            this.baseMetadataHook = baseMetadataHook;
        }

        private void sendHeartBeatMessage(Map<String, Object> heartBeatData) throws ArkletRuntimeException {
            String body = JSONObject.toJSONString(heartBeatData);
            OutputStream in = null;
            try {
                LOGGER.info("sendHeartBeatMessage {}", body);
                HttpURLConnection conn = getHttpURLConnection();
                //获取输出流
                in = conn.getOutputStream();
                in.write(body.getBytes());
                in.flush();
                in.close();
                //取得输入流，并使用Reader读取
                if (200 != conn.getResponseCode()) {
                    LOGGER.error("ResponseCode is an error code:{}", conn.getResponseCode());
                }
            } catch (Exception e) {
                LOGGER.error("sendHeartBeatMessage failed", e);
                throw new ArkletRuntimeException(e);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ioe) {
                    LOGGER.error("Close InputStream failed", ioe);
                }
            }
        }

        private HttpURLConnection getHttpURLConnection() throws IOException {
            URL url = new URL(heartBeatEndpoint + "/heartbeat");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            //发送POST请求必须设置为true
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //设置连接超时时间和读取超时时间
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type", "application/json");
            return conn;
        }

        @Override
        public void run() {
            // send heart beat message
            Map<String, Object> heartBeatData = new HashMap<>();

            heartBeatData.put(BASE_ID, baseID);

            BaseMetadata metadata = baseMetadataHook.getBaseMetadata();
            heartBeatData.put(MASTER_BIZ_INFO, metadata);

            Map<String, Object> networkInfo = new HashMap<>();

            try {
                InetAddress localHost = InetAddress.getLocalHost();
                networkInfo.put(LOCAL_IP, localHost.getHostAddress());
                networkInfo.put(LOCAL_HOST_NAME, localHost.getHostName());
                networkInfo.put(ARKLET_PORT, port);
            } catch (UnknownHostException e) {
                throw new ArkletRuntimeException("get local host failed", e);
            }

            heartBeatData.put(NETWORK_INFO, networkInfo);
            heartBeatData.put(STATE, BizState.ACTIVATED);

            sendHeartBeatMessage(heartBeatData);
        }
    }

}

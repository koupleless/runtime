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

import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletRuntimeException;
import com.alipay.sofa.koupleless.common.log.ArkletLogger;
import com.alipay.sofa.koupleless.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.arklet.core.common.model.BaseMetadata;
import com.alipay.sofa.koupleless.arklet.core.common.model.BaseStatus;
import com.alipay.sofa.koupleless.arklet.core.hook.base.BaseMetadataHook;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class HeartBeatScheduledMission implements Runnable {

    private static final ArkletLogger LOGGER = ArkletLoggerFactory.getDefaultLogger();

    private final String              heartBeatEndpoint;
    private int                       port;
    private final BaseMetadataHook    baseMetadataHook;

    public HeartBeatScheduledMission(int port, String heartBeatEndpoint,
                                     BaseMetadataHook baseMetadataHook) {
        this.port = port;
        this.heartBeatEndpoint = heartBeatEndpoint;
        this.baseMetadataHook = baseMetadataHook;
    }

    private void sendHeartBeatMessage(BaseStatus baseStatus) throws ArkletRuntimeException {
        String body = JSONObject.toJSONString(baseStatus);
        HttpURLConnection conn = null;
        try {
            LOGGER.debug("Heartbeat message sent successfully. {}", body);
            conn = getHttpURLConnection();
            try (OutputStream out = conn.getOutputStream()) {
                out.write(body.getBytes());
                out.flush();
            }
            if (200 != conn.getResponseCode()) {
                LOGGER.error("ResponseCode is an error code:{}", conn.getResponseCode());
            }
        } catch (Exception e) {
            LOGGER.error("sendHeartBeatMessage failed", e);
            throw new ArkletRuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
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
        try {
            // send heart beat message
            BaseMetadata baseMetadata = BaseMetadata.builder().name(baseMetadataHook.getName())
                .identity(baseMetadataHook.getIdentity())
                .clusterName(baseMetadataHook.getClusterName()).build();
            BaseStatus baseStatus = BaseStatus.builder().baseMetadata(baseMetadata)
                .localIP(baseMetadataHook.getLocalIP())
                .localHostName(baseMetadataHook.getLocalHostName())
                .state(BizState.ACTIVATED.getBizState()).port(port).build();

            sendHeartBeatMessage(baseStatus);
        } catch (Exception e) {
            LOGGER.error("Exception occurred during heartbeat execution", e);
        }

    }
}

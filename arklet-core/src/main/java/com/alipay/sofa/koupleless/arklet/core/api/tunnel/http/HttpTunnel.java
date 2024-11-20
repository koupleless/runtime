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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.ark.common.util.PortSelectUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.koupleless.arklet.core.api.tunnel.Tunnel;
import com.alipay.sofa.koupleless.arklet.core.api.tunnel.http.netty.NettyHttpServer;
import com.alipay.sofa.koupleless.arklet.core.command.CommandService;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletInitException;
import com.alipay.sofa.koupleless.arklet.core.common.exception.ArkletRuntimeException;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLogger;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.arklet.core.hook.base.BaseMetadataHook;
import com.google.inject.Singleton;

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
    private ScheduledExecutorService  heartBeatExecutor;

    /** {@inheritDoc} */
    @Override
    public void init(CommandService commandService, BaseMetadataHook baseMetadataHook) {
        if (init.compareAndSet(false, true)) {
            this.commandService = commandService;
            this.baseMetadataHook = baseMetadataHook;
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
                heartBeatExecutor = Executors.newScheduledThreadPool(1);

                heartBeatExecutor.scheduleAtFixedRate(
                    new HeartBeatScheduledMission(port, heartBeatEndpoint, baseMetadataHook), 0,
                    120000L, TimeUnit.MILLISECONDS);
            }

            LOGGER.info("http tunnel initialized: {}", this);
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
                nettyHttpServer.open(baseMetadataHook.getIdentity());
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
                if (heartBeatExecutor != null) {
                    heartBeatExecutor.shutdown();
                    heartBeatExecutor = null;
                }
            } catch (Throwable t) {
                LOGGER.error("An error occurs when shutdown arklet http server.", t);
                throw new ArkletRuntimeException(t);
            }
        }
    }
}

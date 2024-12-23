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
package com.alipay.sofa.koupleless.plugin.manager.handler;

import com.alipay.sofa.ark.spi.event.biz.BeforeBizRecycleEvent;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.selector.ContextSelector;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * <p>StopLoggerContextOnUninstallEventHandler class.</p>
 *
 * @author CoreBoxer
 * @version StopLoggerContextOnUninstallEventHandler.java
 *
 * 使用 context.stop 尝试释放Logger资源(目前仅处理Log4j), 因此监听 BeforeBizRecycleEvent 模块事件
 */
public class StopLoggerContextOnUninstallEventHandler implements
                                                             EventHandler<BeforeBizRecycleEvent> {

    private static final Logger                                                         LOGGER                              = getLogger(
        StopLoggerContextOnUninstallEventHandler.class);

    /** Constant <code>LOGGER_STOP_TIMEOUT_MILLISECONDS="com.alipay.koupleless.logger.stop.timeout.milliseconds"{trunked}</code> */
    public static final String                                                          LOGGER_STOP_TIMEOUT_MILLISECONDS      = "com.alipay.koupleless.logger.stop.timeout.milliseconds";

    /** {@inheritDoc} */
    @Override
    public void handleEvent(BeforeBizRecycleEvent event) {

        ClassLoader bizClassLoader = event.getSource().getBizClassLoader();
        LOGGER.info(
                "[StopLoggerContextOnUninstallEventHandler] Module name: {} , BizClassLoader: {} .",
                event.getSource().getBizName(), bizClassLoader);

        LoggerContextFactory factory = LogManager.getFactory();

        if (factory instanceof Log4jContextFactory) {
            String ctxName = Integer.toHexString(System.identityHashCode(bizClassLoader));
            ContextSelector selector = ((Log4jContextFactory) factory).getSelector();

            int stopTimeoutMilliSeconds = parseInt(getProperty(LOGGER_STOP_TIMEOUT_MILLISECONDS, "500"));
            for (LoggerContext context : selector.getLoggerContexts()) {
                if (context.getName().equals(ctxName)) {
                    LOGGER.info(
                            "[StopLoggerContextOnUninstallEventHandler] {} managed Log4j context found for module: {} . ",
                            ctxName, event.getSource().getBizName());
                    context.stop(stopTimeoutMilliSeconds, TimeUnit.MILLISECONDS);
                }
            }
            return;
        }

        LOGGER.info(
                "[StopLoggerContextOnUninstallEventHandler] No managed Log4j context for module: {}.",
                event.getSource().getBizName());
    }

    /** {@inheritDoc} */
    @Override
    public int getPriority() {
        return LOWEST_PRECEDENCE;
    }
}

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

import com.alipay.sofa.ark.spi.event.biz.BeforeBizStopEvent;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import com.alipay.sofa.common.utils.ClassUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.selector.ContextSelector;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author gaowh
 */
public class StopLoggerCxtAfterBizStopEventHandler implements EventHandler<BeforeBizStopEvent> {

    private static final Logger LOGGER                                  = getLogger(
        StopLoggerCxtAfterBizStopEventHandler.class);

    public static final String LOGGER_CONTEXT_STOP_TIMEOUT_MILLISECOND = "com.alipay.koupleless.loggerContext.stop.timeout.millisecond";
    public static final String LOG4J2_FACTORY_CLASS_NAME = "org.apache.logging.log4j.core.impl.Log4jContextFactory";


    @Override
    public void handleEvent(BeforeBizStopEvent beforeBizStopEvent) {
        if (ClassUtil.isPresent(LOG4J2_FACTORY_CLASS_NAME)) {
            releaseLog4j2LogCtx(beforeBizStopEvent);
        }
    }

    private void releaseLog4j2LogCtx(BeforeBizStopEvent event) {
        try {
            ClassLoader bizClassLoader = event.getSource().getBizClassLoader();
            LoggerContextFactory factory = LogManager.getFactory();
            if (factory instanceof Log4jContextFactory) {
                String ctxName = Integer.toHexString(System.identityHashCode(bizClassLoader));
                ContextSelector selector = ((Log4jContextFactory) factory).getSelector();
                List<LoggerContext> contextList = selector.getLoggerContexts();
                int stopTimeoutMillisecond = parseInt(
                    getProperty(LOGGER_CONTEXT_STOP_TIMEOUT_MILLISECOND, "300"));
                // Traverse the loggerContext of the selector and find the loggerContext that belongs to the bizClassloader module and close it
                for (LoggerContext ctx : contextList) {
                    if (ctx.getName().equals(ctxName)) {
                        boolean stop = ctx.stop(stopTimeoutMillisecond, TimeUnit.MILLISECONDS);
                        LOGGER.info("try stop {}:{}'s logger context {},result={}",
                            event.getSource().getBizName(), event.getSource().getBizVersion(),
                            ctxName, stop);
                    }
                }
                return;
            }
            LOGGER.info("Not Log4jContextFactory, do nothing");
        } catch (Exception exception) {
            Biz source = event.getSource();
            LOGGER.error("release {}:{}'s log4j2LogCtx failed,event id = {}", source.getBizName(),
                source.getBizVersion(), source.getIdentity(), exception);
        }
    }

    // BizUninstallEventHandler will clean bizContext and classloader, The loggerContext needs to be closed before it
    @Override
    public int getPriority() {
        return DEFAULT_PRECEDENCE - 1;
    }
}

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

import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizRecycleEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.selector.ContextSelector;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.apache.logging.slf4j.SLF4JLoggerContextFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.alipay.sofa.koupleless.plugin.manager.handler.StopLoggerContextOnUninstallEventHandler.LOGGER_STOP_TIMEOUT_MILLISECONDS;
import static java.lang.System.clearProperty;
import static java.lang.Thread.currentThread;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author CoreBoxer
 * @version ShutdownExecutorServicesOnUninstallEventHandlerTest.java
 */
public class StopLoggerContextOnUninstallEventHandlerTest {

    private final ClassLoader contextClassLoader = currentThread().getContextClassLoader();

    @Before
    public void setUp() {
        clearProperty(LOGGER_STOP_TIMEOUT_MILLISECONDS);
        currentThread().setContextClassLoader(contextClassLoader);
    }

    @After
    public void tearDown() {
        currentThread().setContextClassLoader(contextClassLoader);
        clearProperty(LOGGER_STOP_TIMEOUT_MILLISECONDS);
    }

    @Test
    public void testStopLoggerContext() {
        setLog4jLoggerContext();

        StopLoggerContextOnUninstallEventHandler handler = new StopLoggerContextOnUninstallEventHandler();
        LoggerContext ctx = (LoggerContext) LogManager.getContext(contextClassLoader, false);
        assertEquals("Expected logger context state to be STARTED", LifeCycle.State.STARTED, ctx.getState());

        BizModel bizModel = new BizModel();
        bizModel.setClassLoader(contextClassLoader);
        BeforeBizRecycleEvent event = new BeforeBizRecycleEvent(bizModel);
        handler.handleEvent(event);
        assertEquals("Expected logger context state to be STOPPED", LifeCycle.State.STOPPED, ctx.getState());

        stopLog4jLoggerContext();
    }

    @Test
    public void testCloseOtherLoggerContext() {
        setSlf4jLoggerContext();

        StopLoggerContextOnUninstallEventHandler handler = new StopLoggerContextOnUninstallEventHandler();
        LoggerContextFactory contextFactory = LogManager.getFactory();
        BizModel bizModel = new BizModel();
        bizModel.setClassLoader(contextClassLoader);
        BeforeBizRecycleEvent event = new BeforeBizRecycleEvent(bizModel);

        handler.handleEvent(event);
        boolean conditionMet = contextFactory instanceof SLF4JLoggerContextFactory;
        assertTrue("SLF4JLoggerContextFactory should be the active logger context factory", conditionMet);
    }

    private void setLog4jLoggerContext() {
        Log4jContextFactory log4jContextFactory = new Log4jContextFactory();
        LogManager.setFactory(log4jContextFactory);
    }

    private void setSlf4jLoggerContext() {
        SLF4JLoggerContextFactory slf4JLoggerContextFactory = new SLF4JLoggerContextFactory();
        LogManager.setFactory(slf4JLoggerContextFactory);
    }

    private void stopLog4jLoggerContext() {
        ContextSelector selector = ((Log4jContextFactory) LogManager.getFactory()).getSelector();
        List<LoggerContext> contextList = selector.getLoggerContexts();
        for (LoggerContext loggerContext : contextList) {
            loggerContext.stop();
        }
    }
}

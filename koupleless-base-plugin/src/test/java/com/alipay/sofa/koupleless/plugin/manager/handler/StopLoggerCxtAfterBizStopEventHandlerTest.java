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
import com.alipay.sofa.ark.spi.event.biz.BeforeBizStopEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.selector.ContextSelector;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.junit.*;

import static com.alipay.sofa.koupleless.plugin.manager.handler.StopLoggerCxtAfterBizStopEventHandler.LOG4J2_FACTORY_CLASS_NAME;
import static com.alipay.sofa.koupleless.plugin.manager.handler.StopLoggerCxtAfterBizStopEventHandler.LOGGER_CONTEXT_STOP_TIMEOUT_MILLISECOND;
import static java.lang.System.clearProperty;
import static java.lang.Thread.currentThread;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockStatic;

import org.mockito.MockedStatic;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

/**
 * @author gaowh
 */
public class StopLoggerCxtAfterBizStopEventHandlerTest {
    private final ClassLoader bizClassLoader = currentThread()
            .getContextClassLoader();
    private static MockedStatic<PropertiesUtil> propertiesUtil;

    private static final ClassLoader rootClassLoader = new ClassLoader() {};

    @BeforeClass
    public static void beforeClass() {
        propertiesUtil = mockStatic(PropertiesUtil.class);
        Properties properties = new Properties();
        properties.setProperty("log4j2.loggerContextFactory", LOG4J2_FACTORY_CLASS_NAME);
        PropertiesUtil mockProperties = new PropertiesUtil(properties);
        propertiesUtil.when(PropertiesUtil::getProperties).thenReturn(mockProperties);
    }

    @AfterClass
    public static void afterClass() {
        if (propertiesUtil != null) {
            propertiesUtil.close();
        }
    }

    @Before
    public void setUp() {
        clearAllLoggerContext();
        clearProperty(LOGGER_CONTEXT_STOP_TIMEOUT_MILLISECOND);
        currentThread().setContextClassLoader(bizClassLoader);
    }

    @After
    public void tearDown() {
        currentThread().setContextClassLoader(bizClassLoader);
        clearProperty(LOGGER_CONTEXT_STOP_TIMEOUT_MILLISECOND);
        clearAllLoggerContext();
    }

    @Test
    public void testCloseLoggerContext() {
        StopLoggerCxtAfterBizStopEventHandler handler = new StopLoggerCxtAfterBizStopEventHandler();
        LogManager.getContext(rootClassLoader, false);
        Log4jContextFactory contextFactory = (Log4jContextFactory) LogManager.getFactory();
        String name = contextFactory.getSelector().getLoggerContexts().get(0).getName();
        LogManager.getContext(bizClassLoader, false);
        assertEquals("Should have two logger context initially", 2, contextFactory.getSelector().getLoggerContexts().size());
        BizModel bizModel = new BizModel();
        bizModel.setClassLoader(bizClassLoader);
        BeforeBizStopEvent event = new BeforeBizStopEvent(bizModel);
        handler.handleEvent(event);
        int afterSize = contextFactory.getSelector().getLoggerContexts().size();
        assertEquals("only one logger context should be closed", afterSize, 1);
        assertEquals("active logger context name is ", name, contextFactory.getSelector().getLoggerContexts().get(0).getName());
    }

    @Test
    public void testCloseLoggerContextWithTimeout() throws URISyntaxException {
        System.setProperty(LOGGER_CONTEXT_STOP_TIMEOUT_MILLISECOND, "2");
        testCloseLoggerContext();
    }

    @Test
    public void testHandleNullClassLoader() {
        StopLoggerCxtAfterBizStopEventHandler handler = new StopLoggerCxtAfterBizStopEventHandler();
        LogManager.getContext(bizClassLoader, false);
        Log4jContextFactory contextFactory = (Log4jContextFactory) LogManager.getFactory();
        int beforeSize = contextFactory.getSelector().getLoggerContexts().size();
        assertEquals("Should have one logger context initially", beforeSize, 1);
        BizModel bizModel = new BizModel();
        // Don't set ClassLoader
        BeforeBizStopEvent event = new BeforeBizStopEvent(bizModel);
        handler.handleEvent(event);
        int afterSize = contextFactory.getSelector().getLoggerContexts().size();
        assertEquals("All logger contexts should be active", afterSize, 1);
    }

    private void clearAllLoggerContext() {
        ContextSelector selector = ((Log4jContextFactory) LogManager.getFactory()).getSelector();
        List<LoggerContext> contextList = selector.getLoggerContexts();
        for (LoggerContext loggerContext : contextList) {
            loggerContext.stop();
        }
    }

}

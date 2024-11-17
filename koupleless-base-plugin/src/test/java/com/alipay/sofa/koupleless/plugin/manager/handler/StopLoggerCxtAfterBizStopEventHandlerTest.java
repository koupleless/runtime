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
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.selector.ContextSelector;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.alipay.sofa.koupleless.plugin.manager.handler.StopLoggerCxtAfterBizStopEventHandler.LOG4J2_FACTORY_CLASS_NAME;
import static com.alipay.sofa.koupleless.plugin.manager.handler.StopLoggerCxtAfterBizStopEventHandler.LOGGER_CONTEXT_STOP_TIMEOUT_MILLISECOND;
import static java.lang.System.clearProperty;
import static java.lang.Thread.currentThread;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mockStatic;

import com.alipay.sofa.common.utils.ClassUtil;
import org.mockito.MockedStatic;

import java.util.Properties;

/**
 * @author gaowh
 */
public class StopLoggerCxtAfterBizStopEventHandlerTest {
    private final ClassLoader              originalClassLoader = currentThread()
        .getContextClassLoader();
    private static MockedStatic<PropertiesUtil> propertiesUtil;

    @BeforeClass
    public static void beforeClass() {
        propertiesUtil = mockStatic(PropertiesUtil.class);
        Properties properties = new Properties();
        properties.setProperty("log4j2.loggerContextFactory", LOG4J2_FACTORY_CLASS_NAME);
        PropertiesUtil mockProperties = new PropertiesUtil(properties);
        propertiesUtil.when(PropertiesUtil::getProperties).thenReturn(mockProperties);
    }

    @Before
    public void setUp() {
        clearProperty(LOGGER_CONTEXT_STOP_TIMEOUT_MILLISECOND);
        currentThread().setContextClassLoader(originalClassLoader);
    }

    @After
    public void tearDown() {
        currentThread().setContextClassLoader(originalClassLoader);
        clearProperty(LOGGER_CONTEXT_STOP_TIMEOUT_MILLISECOND);
    }

    @Test
    public void testCloseLoggerContext() {
        StopLoggerCxtAfterBizStopEventHandler handler = new StopLoggerCxtAfterBizStopEventHandler();
        LogManager.getContext(originalClassLoader, false);
        Log4jContextFactory contextFactory = (Log4jContextFactory)LogManager.getFactory();
        int beforeSize = contextFactory.getSelector().getLoggerContexts().size();
        assertEquals(beforeSize,1);
        BizModel bizModel = new BizModel();
        bizModel.setClassLoader(originalClassLoader);
        BeforeBizStopEvent event = new BeforeBizStopEvent(bizModel);
        handler.handleEvent(event);
        int afterSize = contextFactory.getSelector().getLoggerContexts().size();
        assertEquals(afterSize, 0);
    }

}
